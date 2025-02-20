package ApiCommonModel::Model::JbrowseDnaSeqTracks;

use strict;
use lib $ENV{GUS_HOME} . "/lib/perl";
use JSON;
use ApiCommonModel::Model::JBrowseUtil;
use ApiCommonModel::Model::JBrowseTrackConfig::SingleCoverageTrackConfig;
use ApiCommonModel::Model::JBrowseTrackConfig::AlignmentsTrackConfig;
use ApiCommonModel::Model::JBrowseTrackConfig::DatasetConfig;
use ApiCommonModel::Model::JBrowseTrackConfig::BigWigStore;
use Data::Dumper;
use URI::Escape;

sub processOrganism {
  my ($organismAbbrev, $projectName, $buildNumber, $webservicesDir, $applicationType, $result) = @_;

  my $jbrowseUtil = ApiCommonModel::Model::JBrowseUtil->new({projectName => $projectName, organismAbbrev => $organismAbbrev, buildNumber => $buildNumber, webservicesDir => $webservicesDir, fileName => "_dnaSeqCache.json"});

  my $datasetProps = $jbrowseUtil->getDatasetProperties();

  my $result = {"tracks" => [] };

  my $publicAbbrevForFiles = $datasetProps->{organism}->{organismNameForFiles};


  my $dnaSeqDatasets = $datasetProps->{dnaseq} ? $datasetProps->{dnaseq} : {};

  foreach my $dataset (keys %$dnaSeqDatasets) {
    my $summary = $dnaSeqDatasets->{$dataset}->{summary};

    my $hasCnvData = $dnaSeqDatasets->{$dataset}->{hasCNVData};
    my $shortAttribution = $dnaSeqDatasets->{$dataset}->{shortAttribution};
    my $studyDisplayName = $dnaSeqDatasets->{$dataset}->{datasetDisplayName};

    my $category = $dnaSeqDatasets->{$dataset}->{category};
    my $subCategory = $dnaSeqDatasets->{$dataset}->{subCategory};

    my ($study) = $dataset =~ /${organismAbbrev}_HTS_SNP_(.+)_RSRC$/;


    foreach my $sampleName(keys %{$dnaSeqDatasets->{$dataset}->{sampleNames}}) {

      #my $baseUrl = "/a/service/jbrowse/store?data=";

      my $relativePathToBamFile = "$publicAbbrevForFiles/bam/$study/$sampleName/result.bam";

      my $copyNumberDataset = "${organismAbbrev}_copyNumberVariations_${study}_RSRC";
      my $relativePathToCnvBWFile = "$publicAbbrevForFiles/bigwig/$copyNumberDataset/$sampleName.bw";

      my $relativePathToBigwigFile = "$publicAbbrevForFiles/bigwig/${study}/${sampleName}/result.bw";

      my $displayName = "$sampleName Coverage and Alignments";
      my $label = "$sampleName Coverage and Alignments";

      my $datasetConfig = ApiCommonModel::Model::JBrowseTrackConfig::DatasetConfig->new({ dataset_name => $dataset,
											  study_display_name => $studyDisplayName,
											  category => $category,
											  subcategory => $subCategory,
											  attribution => $shortAttribution,
											  summary => $summary,
											  application_type => $applicationType,
											  organism_abbrev => $organismAbbrev,
											});


      my $alignment = ApiCommonModel::Model::JBrowseTrackConfig::AlignmentsTrackConfig->new({dataset_config => $datasetConfig,
											     project_name => $projectName,
											     build_number => $buildNumber,
											     relative_path_to_file => $relativePathToBamFile,
											     #url_template => $bamUrl,
											     display_name => $displayName,
											     application_type => $applicationType,
											     label => $label,
											     bw_relative_path_to_file => $relativePathToBigwigFile,
											     key => "$sampleName Coverage and Alignments",
											    })->getConfigurationObject();

      $displayName = $sampleName;
      my $displayNameSuffix = " Coverage normalised to chromosome copy number (ploidy)";
      $label = "${dataset}_$sampleName";

      my $cnvCoverage = ApiCommonModel::Model::JBrowseTrackConfig::SingleCoverageTrackConfig::CNV->new({ dataset_config => $datasetConfig,
													 project_name => $projectName,
													 build_number => $buildNumber,
													 relative_path_to_file => $relativePathToCnvBWFile,
													 #url_template => $cnvBWUrl,
													 display_name => $displayName,
													 display_name_suffix => $displayNameSuffix,
													 application_type => $applicationType,
													 label => $label,
													 cov_max_score_default => 5,
													 scale => 'linear',
													 clip_marker_color => 'red',
												       })->getConfigurationObject();


      push @{$result->{tracks}}, $alignment;
      push @{$result->{tracks}}, $cnvCoverage if(($cnvCoverage && lc $hasCnvData eq "true") &&
						 !($dataset eq 'afumAf293_HTS_SNP_Verweij_IsogenicStrains_RSRC' && $sampleName eq 'V157-47'));
    }

  }

  print encode_json($result);
  print CACHE encode_json($result);
  close CACHE;
}

1;

