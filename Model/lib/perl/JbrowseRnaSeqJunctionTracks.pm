package ApiCommonModel::Model::JbrowseRnaSeqJunctionTracks;

use strict;
use lib $ENV{GUS_HOME} . "/lib/perl";
use JSON;
use ApiCommonModel::Model::JBrowseUtil;
use ApiCommonModel::Model::JBrowseTrackConfig::RNASeqJunctionTrackConfig;
use ApiCommonModel::Model::JbrowseRnaSeqJunctionTracks;

use Storable 'dclone';

sub processOrganism {
  my ($organismAbbrev, $projectName, $isApollo, $applicationType) = @_;

  my $jbrowseUtil;

  if ($isApollo) {
    $jbrowseUtil = ApiCommonModel::Model::JBrowseUtil->new({projectName => $projectName, organismAbbrev => $organismAbbrev});
  } else {
    $jbrowseUtil = ApiCommonModel::Model::JBrowseUtil->new({projectName => $projectName, organismAbbrev => $organismAbbrev, fileName => "_junctionsCache.json"});
  }

  # we're done if we can get from cache file (unless we are making apollo config)
  if (!$isApollo && $jbrowseUtil->printFromCache()) {
    exit 0;
  }

  my $methodDescription = "<h1><u>Definitions</u>:</h1>

<p><b>Contained intron</b> is an intron that is contained within the endpoints of a gene model and on the same strand as the gene. All others are not contained: opposite strand to gene, overlapping but not contained within gene boundaries, not overlapping with gene.</p>

<p><b>Score</b>: the sum of all unique read alignments for all samples.</p>

<p><b>Score expression ratio</b>: ratio of the score / average coverage (read depth) across the gene.  For not contained introns, the average coverage is the average coverage across the entire genome.  Note that this is computed per sample (both the score and coverage).</p>

<h1><u>Thresholds used (all are applied)</u>:</h1>

<ol>
<li>Intron length must  be less than 2.5 times the maximum length of any annotated intron for this genome.  For example, if the maximum intron length is 100 KB then all introns shown must be less than 250 KB.</li>

<li>The score >=</li>

<ul>
<li>contained intron: first percentile of all annotated intron scores</li>

<li>not contained: 5 times the first percentile of annotated intron scores</li>
</ul>
<li>For contained introns: score must be >= 2 percent of the score of the intron in this gene with the highest score.</li>

<li>score expression ratio must be greater than or equal to</li>

<ul>
<li>contained intron: minimum (annotated score expression ratio)</li>
<li>not contained: 5th percentile of the annotated score expression ratio.</li>
</ul>
</ol>
 ";
  $methodDescription =~ s/\n//g;


  my $inclusiveQueryParams = $jbrowseUtil->intronJunctionsQueryParams('inclusive');

  my $dbh = $jbrowseUtil->getDbh();   

  my $sql = "select count(*)
from apidbtuning.datasetproperty p
   , apidbtuning.datasetnametaxon d
   , apidb.organism o
where d.DATASET_PRESENTER_ID = p.DATASET_PRESENTER_ID
and o.taxon_id = d.taxon_id
and d.name like '%rnaSeq_RSRC'
and p.property = 'showIntronJunctions'
and lower(p.value) = 'true'
and o.ABBREV = '$organismAbbrev'";

  my $sh = $dbh->prepare($sql);
  $sh->execute();

  my ($count) = $sh->fetchrow_array();
  $sh->finish();

  my $result = {"tracks" => [] };
  if ($count > 0) {

    my $inclusive = ApiCommonModel::Model::JBrowseTrackConfig::RNASeqJunctionTrackConfig->new({application_type => $applicationType,
                                                                                               summary => $methodDescription,
                                                                                              })->getConfigurationObject();

    #  my $inclusive = {storeClass => "JBrowse/Store/SeqFeature/REST",
    #                   baseUrl => "/a/service/jbrowse",
    #                   type => "EbrcTracks/View/Track/CanvasSubtracks",
    ###                   type => "JBrowse/View/Track/CanvasFeatures",
    #                   key => "RNA-Seq Evidence for Introns",
    #                         glyph => "EbrcTracks/View/FeatureGlyph/AnchoredSegment",
    #                   label => "RNA-Seq Evidence for Introns",
    #                   category => "Transcriptomics",
    #                   maxFeatureScreenDensity => 0.5,
    #                   fmtMetaValue_Description => "function(){return datasetDescription(\"$methodDescription\", \"\");}",
    #                   unsafePopup => JSON::true,
    #                   metadata => {
    #                     subcategory => 'RNA-Seq',
    #                     trackType => 'Predicted Intron Junctions',
    #                     description => $methodDescription,
    #                   },
    #                   subtracks => [
    #                     {featureFilters => {
    #                       annotated_intron => "Yes",
    #                       evidence => "Strong Evidence",
    #                      },
    #                      visible => 1,
    #                      label => "Matches Annotation",
    #                      metadata => {},
    #                     },
    #                     {featureFilters => {
    #                       annotated_intron => "No",
    #                       evidence => "Strong Evidence",
    #                      },
    #                      visible => 1,
    #                      label => "Unannotated (Strong Evidence)",
    #                      metadata => {},
    #                     },
    #                     {featureFilters => {
    #                       annotated_intron => "No",
    #                       evidence => "Weak Evidence",
    #                      },
    #                      visible => 0,
    #                      label => "Unannotated (Weak Evidence)",
    #                      metadata => {},
    #                     },
    #                       ],
    #                   displayMode => "normal",
    #                   style => {
    #                     postHeight => "{gsnapIntronPostHeight}",
    #                     lineThickness => "{gsnapIntronLineThickness}",
    #                     color => "{gsnapIntronColorFromStrandAndScore}",
    #                     showLabels => JSON::false,
    #                     label => "function(f){return \"Reads=\" + f.get(\"score\");}",
    #                     strandArrow => JSON::false,
    #                   },
    #
    #                   query => {
    #                     feature => "gsnap:unifiedintronjunctionnew",
    #                   },
    #                   onClick => {
    #                      action => "iframeDialog",
    #                      hideIframeDialogUrl =>  JSON::true,
    #                      url => "/a/app/embed-record/junction/{name}?tables=SampleInfo"
    #                   },
    #                   menuTemplate => [
    #                     {label => "View Details", 
    #                      content => "{gsnapUnifiedIntronJunctionTitleFxn}",
    #                     },
    #                     {label => "Intron Record:  {name}",
    #                      action => "iframeDialog",
    #                      hideIframeDialogUrl =>  JSON::true,
    #                      url => "/a/app/embed-record/junction/{name}?tables=SampleInfo"},
    #                     {label => "Highlight this Feature"},
    #                       ],
    #  };

    push @{$result->{tracks}}, $inclusive;

    if ($isApollo) {

      my $refinedNovel = dclone $inclusive;
      $refinedNovel->{category} = "Draggable Annotation";
      $refinedNovel->{type} = "JBrowse/View/Track/HTMLFeatures";
      $refinedNovel->{key} = $refinedNovel->{key} . " Novel with Strong Evidence";
      $refinedNovel->{label} = $refinedNovel->{label} . " Novel with Strong Evidence";
      $refinedNovel->{query}->{annotated_intron} = "No";
      $refinedNovel->{query}->{feature} = "gsnap:unifiedintronjunctionHCOnly";
      delete $refinedNovel->{onClick};

      my $inclusiveNovel = dclone $inclusive;
      $inclusiveNovel->{category} = "Draggable Annotation";
      $inclusiveNovel->{type} = "JBrowse/View/Track/HTMLFeatures";
      $inclusiveNovel->{key} = $inclusiveNovel->{key} . " Novel with Weak Evidence";
      $inclusiveNovel->{label} = $inclusiveNovel->{label} . " Novel with Weak Evidence";
      $inclusiveNovel->{query}->{annotated_intron} = "No";
      $inclusiveNovel->{query}->{feature} = "gsnap:unifiedintronjunctionLCOnly";
      delete $inclusiveNovel->{onClick};

      my $inclusiveKnown = dclone $inclusive;
      $inclusiveKnown->{category} = "Draggable Annotation";
      $inclusiveKnown->{type} = "JBrowse/View/Track/HTMLFeatures";
      $inclusiveKnown->{key} = $inclusiveKnown->{key} . " Matches Transcript Annotation";
      $inclusiveKnown->{label} = $inclusiveKnown->{label} . " Matches Transcript Annotation";
      $inclusiveKnown->{query}->{annotated_intron} = "Yes";
      $inclusiveKnown->{query}->{feature} = "gsnap:unifiedintronjunctionAnnotatedOnly";
      delete $inclusiveKnown->{onClick};


      push @{$result->{tracks}}, $refinedNovel;
      push @{$result->{tracks}}, $inclusiveNovel;
      push @{$result->{tracks}}, $inclusiveKnown;
    }
  }

  $dbh->disconnect();
  print encode_json($result);

  unless($isApollo) {
    open(CACHE, "> " . $jbrowseUtil->getCacheFile()) or die "Cannot open file " . $jbrowseUtil->getCacheFile() . " for writing: $!";
    print CACHE encode_json($result);
    close CACHE;
  }
}
1;
