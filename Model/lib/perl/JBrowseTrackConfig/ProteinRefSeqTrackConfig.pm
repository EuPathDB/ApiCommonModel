package ApiCommonModel::Model::JBrowseTrackConfig::ProteinRefSeqTrackConfig;
use base qw(ApiCommonModel::Model::JBrowseTrackConfig::Segments);
use strict;
use warnings;

use ApiCommonModel::Model::JBrowseTrackConfig::FastaStore;

sub getGlyph {$_[0]->{glyph} }
sub setGlyph {$_[0]->{glyph} = $_[1]}

sub getUrl {$_[0]->{url} }
sub setUrl {$_[0]->{url} = $_[1]}

sub new {
    my ($class, $args) = @_;
    my $self = $class->SUPER::new($args);

    my $datasetConfig = $self->getDatasetConfigObj();
    $datasetConfig->setCategory("Sequence Analysis");

    $self->setId($args->{key});
    $self->setLabel($args->{label}); 
    $self->setGlyph($args->{glyph});
    $self->setUrl($args->{relative_path_to_file});

    my $store;

    if($self->getApplicationType() eq 'jbrowse' || $self->getApplicationType() eq 'apollo') {
        $store = ApiCommonModel::Model::JBrowseTrackConfig::FastaStore->new($args);
	$store->setQueryParamsHash($args->{query_params});
	$self->setDisplayType("Sequence");
	$self->setTrackTypeDisplay("Reference Sequence");
    }
    else {
        # TODO
    }

    $self->setStore($store);

    return $self;
}


sub getJBrowseObject{
    my $self = shift;

    my $jbrowseObject = $self->SUPER::getJBrowseObject();
    $jbrowseObject->{urlTemplate} = $self->getUrl();
    $jbrowseObject->{seqType} = "protein";
    $jbrowseObject->{useAsRefSeqStore} = "true";
    $jbrowseObject->{unsafePopup} = "true";
    $jbrowseObject->{showReverseStrand} = "false";
    $jbrowseObject->{showTranslation} = "false";
    $jbrowseObject->{proteinColorScheme} = "colorblind";

    return $jbrowseObject;
  }

# TODO:
sub getJBrowse2Object{
        my $self = shift;

        my $jbrowse2Object = $self->SUPER::getJBrowse2Object();


        return $jbrowse2Object;
}


1;
