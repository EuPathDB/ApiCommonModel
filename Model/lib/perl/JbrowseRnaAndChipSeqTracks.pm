package ApiCommonModel::Model::JbrowseRnaAndChipSeqTracks;

use strict;
use lib $ENV{GUS_HOME} . "/lib/perl";
use JSON;
use ApiCommonModel::Model::JBrowseUtil;
use ApiCommonModel::Model::JBrowseTrackConfig::SingleCoverageTrackConfig;
use ApiCommonModel::Model::JBrowseTrackConfig::MultiBigWigTrackConfig;
use ApiCommonModel::Model::JBrowseTrackConfig::BigWigStore;

use Data::Dumper;

use Encode;
use XML::Simple;
use List::Util qw(min max);
use Storable 'dclone';
use JSON qw(decode_json);

sub processOrganism {
  my ($organismAbbrev, $projectName, $buildNumber, $webservicesDir, $applicationType, $technologyType, $result) = @_;

  my $fileName = "_" . $technologyType . "Cache.json";

  # set applicationType, legacyWebappBaseUrl, localhost in the util class
  my $jbrowseUtil = ApiCommonModel::Model::JBrowseUtil->new({projectName => $projectName, organismAbbrev => $organismAbbrev, fileName => $fileName});
  #my $jbrowseUtil = ApiCommonModel::Model::JBrowseUtil->new({projectName => $projectName, organismAbbrev => $organismAbbrev, buildNumber => $buildNumber, webservicesDir => $webservicesDir, fileName => $fileName, applicationType => $applicationType, legacyWebappBaseUrl => $legacyWebappBaseUrl, localhost => $localhost});

  my $datasetProps = $jbrowseUtil->getDatasetProperties();


  # TODO:  Add this back when we are done
  # # we're done if we can get from cache file
  # if($jbrowseUtil->printFromCache()) {
  #   exit 0;
  # }
  #open(CACHE, "> " . $jbrowseUtil->getCacheFile()) or die "Cannot open file " . $jbrowseUtil->getCacheFile() . " for writing: $!";
  #my $dbh = $jbrowseUtil->getDbh();

  ### Get organism properties
  my $orgHash = ($datasetProps->{'organism'});
  my $nameForFileNames = ($orgHash->{organismNameForFiles});
  my $projectName = ($orgHash->{projectName});
  ### Create rnaseq hash and extract datasetName
  my $allDatasetPropertiesForAssayType = ($datasetProps->{lc($technologyType)});

  foreach my $datasetName (keys %$allDatasetPropertiesForAssayType) {
    my %studyProperties = %{$allDatasetPropertiesForAssayType->{$datasetName}};
    next unless($datasetName =~ /_chipSeq_RSRC/ || $datasetName =~ /_rnaSeq_RSRC/);
    my $category = $studyProperties{category};
    my $covMaxScoreDefault = $studyProperties{covMaxScoreDefault};
    my $multiCovScale = $studyProperties{multiCovScale};
    my $subCategory = $studyProperties{subCategory};

    # TODO:  Handle duplicate Horn CDS Dataset Better
    next if($datasetName eq 'tbruTREU927_RNAi_Horn_CDS_rnaSeq_RSRC');

    my $metadataBase = "metadata_unlogged";

    if (lc $studyProperties{isStrandSpecific} eq 'true' && lc $studyProperties{switchStrandsGBrowse} eq 'true') {
      $metadataBase = $metadataBase . "_alt";
    }

    my %refinedQueryParams = %{$jbrowseUtil->intronJunctionsQueryParams('refined')};
    $refinedQueryParams{intronSizeLimit} = $projectName eq 'HostDB' ? 20000 : 3000;

    # EXAMPLE:
    #/var/www/Common/apiSiteFilesMirror/webServices/PlasmoDB/build-42/Pfalciparum3D7/bigwig/pfal3D7_Caro_ribosome_profiling_rnaSeq_RSRC/metadata_unlogged

    my $bigWigRelativePath = "$projectName/build-$buildNumber/$nameForFileNames/bigwig/$datasetName";
    #print $bigWigRelativePath."\n";
    my $metadataFile = "$webservicesDir/$bigWigRelativePath/$metadataBase";
    #print $metadataFile."\n";
    if (! -e $metadataFile) {
      #TODO:  take out the print if everything looks ok
      #print STDERR "SKIPPING dataset $datasetName.  File not found:  $metadataFile";
      next;
    }

    open(FILE, $metadataFile) or die "Cannot open file $metadataFile for reading: $!";

    my ($start, $file, $displayName, $strand, $alignment, $dbid, $sample, $order);;
  
    my @multiUrls;


    my $studyDisplayName = $studyProperties{datasetDisplayName};

    my $shortAttribution = $studyProperties{shortAttribution};
    $shortAttribution =~ s/[()]//g;
    my $summary = $studyProperties{summary};
    $summary =~ s/\n/ /g;

    my $datasetConfig = ApiCommonModel::Model::JBrowseTrackConfig::DatasetConfig->new({ dataset_name => $datasetName,
											study_display_name => $studyDisplayName,
											category => $category,
											subcategory => $subCategory,
											attribution => $shortAttribution,
											summary => $summary,
											organism_abbrev => $organismAbbrev,
										      });

    while (<FILE>) {
      chomp;
      if (/^\[(.+)\]$/) {

	&addConfiguration($result, $file, $displayName, $strand, $alignment, \%studyProperties, $nameForFileNames, $bigWigRelativePath, \@multiUrls, $dbid, $sample, $covMaxScoreDefault, $multiCovScale, $datasetConfig, $datasetName, $summary, $order, $technologyType, $projectName, $buildNumber, $applicationType) if($start);
	$file = $1;
	$start = 1;
      }
      if (/^display_name\s*=\s*(.+)$/) {
	$displayName = $1;
	($order) = $displayName =~ /^(\S+) - /; #capture the order info prior to replace
	$displayName =~ s/^\S+ - //; #remove everything to the first " - "
	$displayName =~ s/ \(\)//;   # remove open/close parens
      }
      if (/^alignment\s*=\s*(.+)$/) {
	$alignment = $1;
      }
      if (/^strand\s*=\s*(.+)$/) {
	$strand = $1;
      }
      if (/^\:dbid\s*=\s*(.+)$/) {
	$dbid = $1;
      }
      if (/^sample\s*=\s*(.+)$/) {
	$sample = $1;
      }
    
    }

    &addConfiguration($result, $file, $displayName, $strand, $alignment, \%studyProperties, $nameForFileNames, $bigWigRelativePath, \@multiUrls, $dbid, $sample, $covMaxScoreDefault, $multiCovScale, $datasetConfig, $datasetName, $summary, $order, $technologyType, $projectName, $buildNumber, $applicationType);

    # ChipSeq are all unique
    my @uniqueOnlyUrls = grep { $technologyType eq 'ChIPSeq' || $_->{alignment} eq 'unique' } @multiUrls;



    my $multiBigWigDensity = ApiCommonModel::Model::JBrowseTrackConfig::MultiBigWigTrackConfig::Density->new({dataset_config => $datasetConfig,
													      dataset_name => $datasetName,
													      project_name => $projectName,
													      build_number => $buildNumber,
													      multi_urls => \@multiUrls,
													      display_name => $displayName,
													      application_type => $applicationType,
													      scale => $multiCovScale,
													      description => $summary,
													      strand => ($strand+1)? 'strand specific' : 'not strand specific',
													      height => max(min(scalar(@multiUrls) * 5, 100), 20),
													     })->getConfigurationObject();



    my $uniqueMultiBigWigDensity = ApiCommonModel::Model::JBrowseTrackConfig::MultiBigWigTrackConfig::Density->new({ dataset_config => $datasetConfig,
														     dataset_name => $datasetName,
														     multi_urls => \@uniqueOnlyUrls,
														     project_name => $projectName,
														     build_number => $buildNumber,
														     display_name => $displayName,
														     application_type => $applicationType,
														     scale => $multiCovScale,
														     alignment => "unique",
														     description => $summary,
														     strand => ($strand+1)? 'strand specific' : 'not strand specific',
														     height => max(min(scalar(@uniqueOnlyUrls) * 5, 100), 20),
														   })->getConfigurationObject();


    my $multiBigWigXYPlot = ApiCommonModel::Model::JBrowseTrackConfig::MultiBigWigTrackConfig::XY->new({ dataset_config => $datasetConfig,
													 dataset_name => $datasetName,
													 project_name => $projectName,
													 build_number => $buildNumber,
													 multi_urls => \@multiUrls,
													 display_name => $displayName,
													 application_type => $applicationType,
													 scale => $multiCovScale,
													 attribution => $shortAttribution,
													 description => $summary,
													 strand => ($strand+1)? 'strand specific' : 'not strand specific',
													 height => 40,
												       })->getConfigurationObject();



    my $uniqueMultiBigWigXYPlot = ApiCommonModel::Model::JBrowseTrackConfig::MultiBigWigTrackConfig::XY->new({ dataset_config => $datasetConfig,
													       dataset_name => $datasetName,
													       multi_urls => \@uniqueOnlyUrls,
													       project_name => $projectName,
													       build_number => $buildNumber,
													       display_name => $displayName,
													       application_type => $applicationType,
													       scale => $multiCovScale,
													       alignment => "unique",
													       description => $summary,
													       strand => ($strand+1)? 'strand specific' : 'not strand specific',
													       height => 40,
													     })->getConfigurationObject();

  

    push @{$result->{tracks}}, $uniqueMultiBigWigDensity if($uniqueMultiBigWigDensity); # ChipSeq are all unique

    if ($technologyType eq 'RNASeq') {
      push @{$result->{tracks}}, $multiBigWigDensity if($multiBigWigDensity);
      push @{$result->{tracks}}, $multiBigWigXYPlot if($multiBigWigXYPlot);
      push @{$result->{tracks}}, $uniqueMultiBigWigXYPlot if($uniqueMultiBigWigXYPlot);
    }
  }
}

sub addConfiguration {
  my ($result, $file, $displayName, $strand, $alignment, $properties, $nameForFileNames, $bigWigRelativePath, $multiUrls, $dbid, $sample, $covMaxScoreDefault, $multiCovScale, $datasetConfig, $datasetName, $summary, $order, $technologyType, $projectName, $buildNumber, $applicationType) = @_;

  my $color;
  if ($technologyType eq 'RNASeq' ){
  $strand = 'not strand specific' unless($strand);
  $color = &color($strand, $alignment, $datasetName, $displayName); 
  }
  else {
  $color = &color($strand, $alignment, $datasetName, $displayName);
  $strand = undef;
  $alignment = undef;
  }

  # old, direct file access; replaced by jbrowse service
#  my $projectUrl = "https://" . lc($projectName) . "\.org";
#  my $bwUrl = "$projectUrl/a/service/jbrowse/store?data=" . uri_escape_utf8("$nameForFileNames/bigwig/$datasetName/$file");
#  my $bwUrl = "/a/service/jbrowse/store?data=" . uri_escape_utf8("$nameForFileNames/bigwig/$datasetName/$file"); 

#  my $color = &color($strand, $alignment, $datasetName, $displayName);

  $dbid = $dbid ? $dbid : ($sample ? $sample : $displayName);
  
  my $displayNameSuffix = "Coverage";

  my $relativePathToFile = "$nameForFileNames/bigwig/$datasetName/$file";

  my $coverageObj = ApiCommonModel::Model::JBrowseTrackConfig::SingleCoverageTrackConfig->new({ dataset_config => $datasetConfig,
                                                                                                project_name => $projectName,
                                                                                                build_number => $buildNumber,
                                                                                                relative_path_to_file => $relativePathToFile,
                                                                                                display_name => $displayName,
												display_name_suffix => $displayNameSuffix, 
                                                                                                application_type => $applicationType,
                                                                                                color => $color,
                                                                                                dbid => $dbid,
												cov_min_score_default => 0,
                                                                                                cov_max_score_default => $covMaxScoreDefault,
                                                                                                scale => $multiCovScale,
                                                                                                strand =>  $strand,
                                                                                                alignment => $alignment,
                                                                                                description => $summary,
												order => $order,
												clip_marker_color => 'black',
                                                                                              })->getConfigurationObject();


  my $multiUrlHash = {relative_path_to_file => $relativePathToFile, name => $displayName, color => $color, alignment => $alignment};

  push @{$result->{tracks}}, $coverageObj if($coverageObj);
  push @$multiUrls, $multiUrlHash;
}


sub color {
  my ($strand, $alignment, $datasetName, $displayName) = @_;

  if($datasetName eq 'tbruTREU927_Rijo_Circadian_Regulation_rnaSeq_RSRC') {
    return &customColorTbCircadianRegulation($strand, $alignment, $datasetName, $displayName);
  }

  if($alignment eq 'unique') {
    if($strand eq 'reverse') {
      return 'red';
    }
    elsif($strand eq 'forward') {
      return 'blue';
    }
    else {
      return '#DC7633';
    }
  }

  return 'grey';;
}

# special colors for a tbru dataset
sub customColorTbCircadianRegulation {
  my ($strand, $alignment, $datasetName, $displayName) = @_;

  if($alignment eq 'non-unique') {
    return 'grey';
  }
  if($displayName =~ /BSF.+Alt/) {
    return "cyan";
  }
  if($displayName =~ /BSF.+Const/) {
    return "goldenrod";
  }
  if($displayName =~ /PF.+Alt/) {
    return "blue";
  }
  if($displayName =~ /PF.+Const/) {
    return "red";
  }
  return "black";
}


1;

