package ApiCommonModel::Model::JBrowseTrackConfig::SignalPeptideTrackConfig;
use base qw(ApiCommonModel::Model::JBrowseTrackConfig::Segments);
use strict;
use warnings;

use ApiCommonModel::Model::JBrowseTrackConfig::GFFStore;

sub getGlyph {$_[0]->{glyph} }
sub setGlyph {$_[0]->{glyph} = $_[1]}

sub getUrl {$_[0]->{url} }
sub setUrl {$_[0]->{url} = $_[1]}



sub new {
    my ($class, $args) = @_;
    my $self = $class->SUPER::new($args);

    my $datasetConfig = $self->getDatasetConfigObj();
    $datasetConfig->setCategory("Protein targeting and localization");

    $self->setId($args->{key});
    $self->setLabel($args->{label}); 
    $self->setGlyph($args->{glyph});
    $self->setUrl($args->{relative_path_to_file});

    my $store;

    if($self->getApplicationType() eq 'jbrowse' || $self->getApplicationType() eq 'apollo') {
        $store = ApiCommonModel::Model::JBrowseTrackConfig::GFFStore->new($args);
	$store->setQueryParamsHash($args->{query_params});
    }
    else {
        # TODO
    }

    $self->setStore($store);
    
    $self->setGlyph("JBrowse/View/FeatureGlyph/Box") unless(defined $self->getGlyph());

    my $detailsFunction = "{signalpTitleFxn}";

    $self->setOnClickContent($detailsFunction);
    $self->setViewDetailsContent($detailsFunction);
#    $self->setDisplayMode("compact");

    return $self;
}

sub getJBrowseStyle {
    my $self = shift;

    my $jbrowseStyle = $self->SUPER::getJBrowseStyle();

    $jbrowseStyle->{color} = "navy";
    $jbrowseStyle->{label} = "NA";

    return $jbrowseStyle;
}


sub getJBrowseObject{
    my $self = shift;

    my $jbrowseObject = $self->SUPER::getJBrowseObject();
    $jbrowseObject->{urlTemplate} = $self->getUrl();

    return $jbrowseObject;
  }

# TODO:
sub getJBrowse2Object{
        my $self = shift;

        my $jbrowse2Object = $self->SUPER::getJBrowse2Object();


        return $jbrowse2Object;
}


1;
