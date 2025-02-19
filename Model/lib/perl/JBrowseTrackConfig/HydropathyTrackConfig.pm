package ApiCommonModel::Model::JBrowseTrackConfig::HydropathyTrackConfig;
use base qw(ApiCommonModel::Model::JBrowseTrackConfig::Segments);
use strict;
use warnings;

use ApiCommonModel::Model::JBrowseTrackConfig::BigWigStore;

sub getUrl {$_[0]->{url} }
sub setUrl {$_[0]->{url} = $_[1]}

sub getPosColor {$_[0]->{pos_color}}
sub setPosColor {$_[0]->{pos_color} = $_[1]}

sub new {
    my ($class, $args) = @_;
    my $self = $class->SUPER::new($args);

    my $datasetConfig = $self->getDatasetConfigObj();
    $datasetConfig->setCategory("Structure analysis");

    $self->setId($args->{key});
    $self->setLabel($args->{label}); 
    $self->setUrl($args->{relative_path_to_file});
    $self->setPosColor($args->{pos_color});

    my $store;

    if($self->getApplicationType() eq 'jbrowse' || $self->getApplicationType() eq 'apollo') {
        $store = ApiCommonModel::Model::JBrowseTrackConfig::BigWigStore->new($args);
	$store->setQueryParamsHash($args->{query_params});

	$self->setDisplayType("JBrowse/View/Track/Wiggle/XYPlot");
        $self->setTrackTypeDisplay("XYPlot");

    }
    else {
        # TODO
    }

    $self->setStore($store);

    return $self;
}

sub getJBrowseStyle {
    my $self = shift;

    my $jbrowseStyle = $self->SUPER::getJBrowseStyle();

    $jbrowseStyle->{pos_color} = $self->getPosColor();
    $jbrowseStyle->{neg_color} = "navy";
    $jbrowseStyle->{height} = 40;
    $jbrowseStyle->{label} = "NA";

    return $jbrowseStyle;
}


sub getJBrowseObject{
    my $self = shift;

    my $jbrowseObject = $self->SUPER::getJBrowseObject();
    $jbrowseObject->{urlTemplate} = $self->getUrl();
    $jbrowseObject->{yScalePosition} = "left";
    $jbrowseObject->{min_score} = "-4.5";
    $jbrowseObject->{max_score} = "4.5";


    return $jbrowseObject;
  }

# TODO:
sub getJBrowse2Object{
        my $self = shift;

        my $jbrowse2Object = $self->SUPER::getJBrowse2Object();


        return $jbrowse2Object;
}


1;
