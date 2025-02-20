package ApiCommonModel::Model::JBrowseTrackConfig::CoverageTrackConfig;
use base qw(ApiCommonModel::Model::JBrowseTrackConfig::TrackConfig);

use strict;
use warnings;

use ApiCommonModel::Model::JBrowseTrackConfig::BigWigStore;

sub getCovMaxScoreDefault {$_[0]->{cov_max_score_default} || 1000}
sub setCovMaxScoreDefault {$_[0]->{cov_max_score_default} = $_[1] + 0}

sub getClipMarkerColor {$_[0]->{clip_marker_color} }
sub setClipMarkerColor {$_[0]->{clip_marker_color} = $_[1]}

sub getCovMinScoreDefault {
    my $self = shift;

    if(defined($self->{cov_min_score_default})) {
      return $self->{cov_min_score_default};
    }
    return $self->getScale eq "log" ? 1 : 0;
}
sub setCovMinScoreDefault {$_[0]->{cov_min_score_default} = $_[1]}

sub getScale {$_[0]->{scale} || "log"}
sub setScale {$_[0]->{scale} = $_[1]}

sub getYScalePosition {$_[0]->{yscale_position} || "left"}
sub setYScalePosition {$_[0]->{yscale_position} = $_[1]}

# These are optional  metadata
sub getStrand {$_[0]->{strand}}
sub setStrand {$_[0]->{strand} = $_[1]}

sub getAlignment {$_[0]->{alignment}}
sub setAlignment {$_[0]->{alignment} = $_[1]}


sub new {
    my ($class, $args) = @_;
    my $self = $class->SUPER::new($args);

#    $self->setScale($args->{scale});

    my $store = ApiCommonModel::Model::JBrowseTrackConfig::BigWigStore->new($args);
    $self->setStore($store);

    if($self->getApplicationType() eq 'jbrowse' || $self->getApplicationType() eq 'apollo') {
        $self->setDisplayType("JBrowse/View/Track/Wiggle/XYPlot");
        $self->setTrackTypeDisplay("Coverage");
    }
    elsif ($self->getApplicationType() eq 'jbrowse2'){
        $self->setTrackType("QuantitativeTrack");
        $self->setDisplayType("LinearWiggleDisplay");

        $self->setTrackTypeDisplay("Coverage");
    } else {
      die "Invalid application type '$self->getApplicationType()'\n";
    }

    # These are optional
    $self->setStrand($args->{strand});
    $self->setAlignment($args->{alignment});

    $self->setHeight(40) unless(defined $self->getHeight());
    $self->setColor("black") unless(defined $self->getColor());
    $self->setClipMarkerColor("red") unless(defined $self->getClipMarkerColor());

    return $self;
}

sub getJBrowseObject{
    my $self = shift;

    my $jbrowseObject = $self->SUPER::getJBrowseObject();
    my $yScalePosition = $self->getYScalePosition();

    $jbrowseObject->{yScalePosition} = $yScalePosition;

    return $jbrowseObject;
}

sub getJBrowse2Object{
    my $self = shift;

    my $jbrowse2Object = $self->SUPER::getJBrowse2Object();

    my $scale = $self->getScale();
    my $minScore = $self->getCovMinScoreDefault();
    my $maxScore = $self->getCovMaxScoreDefault();
    #my $yScalePosition = $self->getYScalePosition();

    # set a floor of 1 for log data
    if($scale eq 'log' && $minScore <1) {
      $minScore = 1;
    }

    $jbrowse2Object->{displays}->[0]->{minScore} = $minScore;
    $jbrowse2Object->{displays}->[0]->{maxScore} = $maxScore;
    $jbrowse2Object->{displays}->[0]->{scaleType} = $scale;

    return $jbrowse2Object;
}



sub getMetadata {
    my $self = shift;

    my $strand = $self->getStrand();
    my $alignment = $self->getAlignment();

    my $metadata = $self->SUPER::getMetadata();
    $metadata->{strand} = $strand if($strand);
    $metadata->{alignment} = $alignment if($alignment);

    return $metadata;
}





1;
