package ApiCommonModel::Model::JBrowseTrackConfig::BedStore;
use base qw(ApiCommonModel::Model::JBrowseTrackConfig::Store);
use strict;
use warnings;

sub getUrlTemplate {$_[0]->{url_template} }
sub setUrlTemplate {$_[0]->{url_template} = $_[1]}

sub getQueryParamsHash {$_[0]->{query_params_hash}}
sub setQueryParamsHash {$_[0]->{query_params_hash} = $_[1] }

sub getQuery {$_[0]->{query}}
sub setQuery {$_[0]->{query} = $_[1] }

sub new {
    my ($class, $args) = @_;
    my $self = $class->SUPER::new($args);

    $self->setUrlTemplate($args->{url_template});

    if($self->getApplicationType() eq 'jbrowse' || $self->getApplicationType() eq 'apollo') {
        $self->setStoreType("JBrowse/Store/SeqFeature/BEDTabix");
    }
    else {
        #die "No GFF Equivalent For JBrowse2 YET";
        $self->setStoreType("BEDTabixAdapter");
    }

    return $self;
}


1;
