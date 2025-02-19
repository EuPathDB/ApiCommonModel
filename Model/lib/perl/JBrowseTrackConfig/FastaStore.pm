package ApiCommonModel::Model::JBrowseTrackConfig::FastaStore;
use base qw(ApiCommonModel::Model::JBrowseTrackConfig::Store);
use strict;
use warnings;

sub getBaseUrl {$_[0]->{base_url}}
sub setBaseUrl {$_[0]->{base_url} = $_[1] }

sub getUrlTemplate {$_[0]->{url_template} }
sub setUrlTemplate {$_[0]->{url_template} = $_[1]}

sub getQueryParamsHash {$_[0]->{query_params_hash}}
sub setQueryParamsHash {$_[0]->{query_params_hash} = $_[1] }

sub new {
    my ($class, $args) = @_;
    my $self = $class->SUPER::new($args);

    $self->setBaseUrl("/a/service/jbrowse");
    $self->setUrlTemplate($args->{url_template});

    if($self->getApplicationType() eq 'jbrowse' || $self->getApplicationType() eq 'apollo') {
        $self->setStoreType("JBrowse/Store/SeqFeature/IndexedFasta");
    }
    else {
        #die "No GFF Equivalent For JBrowse2 YET";
        $self->setStoreType("IndexedFasta");
    }

    return $self;
}


1;
