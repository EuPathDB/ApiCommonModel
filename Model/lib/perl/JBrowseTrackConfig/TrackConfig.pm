package ApiCommonModel::Model::JBrowseTrackConfig::TrackConfig;
use base qw(ApiCommonModel::Model::JBrowseTrackConfig::JBrowseBaseConfig);

use ApiCommonModel::Model::JBrowseTrackConfig::DatasetConfig;

use strict;
use warnings;

use Data::Dumper;
use URI::Escape;
use Encode;

sub getLabel {$_[0]->{label}}
sub setLabel {$_[0]->{label} = $_[1]}

sub getId {$_[0]->{id}}
sub setId {$_[0]->{id} = $_[1]}

sub getStore {$_[0]->{store}}
sub setStore {$_[0]->{store} = $_[1]}

sub getDisplayType {$_[0]->{display_type}}
sub setDisplayType {$_[0]->{display_type} = $_[1]}

sub getTrackType {$_[0]->{track_type}}
sub setTrackType {$_[0]->{track_type} = $_[1]}

sub getTrackTypeDisplay {$_[0]->{track_type_display} || $_[0]->getTrackType()}
sub setTrackTypeDisplay {$_[0]->{track_type_display} = $_[1]}

# Some basic style things
sub getColor {$_[0]->{color}}
sub setColor {$_[0]->{color} = $_[1]}

sub getHeight {$_[0]->{height}}
sub setHeight {$_[0]->{height} = $_[1]}

sub getDisplayName {$_[0]->{display_name}}
sub setDisplayName {$_[0]->{display_name} = $_[1]}

# this is the track description if we need one
sub getDescription {$_[0]->{description}}
sub setDescription {$_[0]->{description} = $_[1]}


# These are optional  metadata
sub getGeneLegend {$_[0]->{gene_legend}}
sub setGeneLegend {$_[0]->{gene_legend} = $_[1]}

sub getRegionLegend {$_[0]->{region_legend}}
sub setRegionLegend {$_[0]->{region_legend} = $_[1]}

sub new {
    my ($class, $args) = @_;

    my $self = $class->SUPER::new($args);


    $self->setDisplayName($args->{display_name});

    $self->setLabel($args->{label});

    $self->setId($args->{id});

    $self->setColor($args->{color});
    $self->setHeight($args->{height});

#    $self->setStoreType($args->{store});
    $self->setStore($args->{store});

    $self->setDescription($args->{description});

    # These are optional
    $self->setGeneLegend($args->{gene_legend});
    $self->setRegionLegend($args->{region_legend});

    return $self;
}


sub getJBrowseStyle {
    my $self = shift;

    my $color = $self->getColor();
    my $height = $self->getHeight();
    my $borderColor = $self->getBorderColor();

    return undef unless(defined($color) || defined($height || $borderColor));

    my $style = {};

    $style->{color} = $color if($color);
    $style->{height} = $height if($height);
    $style->{border_color} = $borderColor if($borderColor);

    return $style;
}


sub getMetadata {
    my $self = shift;


    my $datasetConfig = $self->getDatasetConfigObj();

    my $subcategory = $datasetConfig->getSubcategory() if ($datasetConfig);
    my $trackType = $self->getTrackTypeDisplay();
    my $attribution = $datasetConfig->getAttribution() if ($datasetConfig);
    $attribution = Encode::decode('utf8', uri_unescape($attribution));
    my $summary = $datasetConfig->getSummary() if ($datasetConfig);
    $summary = Encode::decode('utf8', uri_unescape($summary));
    my $studyDisplayName = $datasetConfig->getStudyDisplayName() if ($datasetConfig);

    my $geneLegend = $self->getGeneLegend();
    my $regionLegend = $self->getRegionLegend();

    unless(defined($subcategory) || defined($trackType)
           || defined($attribution) || defined($summary) || defined($studyDisplayName)) {
        return undef;
    }

    my $metadata = {};
    $metadata->{subcategory} = $subcategory if($subcategory);
    $metadata->{trackType} = $trackType if($trackType);
    $metadata->{attribution} = $attribution if($attribution);
    $metadata->{dataset} = $studyDisplayName if($studyDisplayName);
    $metadata->{description} = $summary if($summary);
    $metadata->{GeneLegend} = $geneLegend if($geneLegend);
    $metadata->{RegionLegend} = $regionLegend if($regionLegend);
    return $metadata;
}

sub getJBrowseObject{
    my $self = shift;

    my $datasetConfig = $self->getDatasetConfigObj();

    my $datasetName = $datasetConfig->getDatasetName() if ($datasetConfig);
    my $studyDisplayName = $datasetConfig->getStudyDisplayName() if ($datasetConfig);
    my $summary = $datasetConfig->getSummary() if ($datasetConfig);
    $summary = Encode::decode('utf8', uri_unescape($summary));
    my $datasetPresenterId = $datasetConfig->getDatasetPresenterId() if ($datasetConfig);

    my $style = $self->getJBrowseStyle();
    my $metadata = $self->getMetadata();

    my $id = $self->getId();
    my $storeType = $self->getStore()->getStoreType();
    my $label = $self->getLabel();
    my $displayType = $self->getDisplayType();
    my $category = $datasetConfig->getCategory() if ($datasetConfig);

    unless($id && $storeType && $label && $displayType && $category) {
        print Dumper $self;
        die "missing a required property (id, storeClass,label, displayType, category)";
    }

    my $jbrowseObject = {storeClass => $storeType,
                         key => $id,
                         label => $label,
                         type => $displayType,
                         category => $category,
    };

    $jbrowseObject->{style} = $style if($style);
    $jbrowseObject->{metadata} = $metadata if($metadata);

    if($datasetName && $studyDisplayName && $summary && $datasetPresenterId) {
        $jbrowseObject->{fmtMetaValue_Dataset} = "function() { return datasetLinkByDatasetId('${datasetPresenterId}', '${studyDisplayName}'); }";
        $jbrowseObject->{fmtMetaValue_Description} = "function() { return datasetDescription('${summary}', ''); }";
    }
    elsif($datasetName && $studyDisplayName && $summary) {
        $jbrowseObject->{fmtMetaValue_Dataset} = "function() { return datasetLinkByDatasetName('${datasetName}', '${studyDisplayName}'); }";
        $jbrowseObject->{fmtMetaValue_Description} = "function() { return datasetDescription('${summary}', ''); }";
    }
    elsif($datasetName && $studyDisplayName) {
        $jbrowseObject->{fmtMetaValue_Dataset} = "function() { return datasetLinkByDatasetName('${datasetName}', '${studyDisplayName}'); }";
        $jbrowseObject->{fmtMetaValue_Description} = "function() { return datasetDescription('', ''); }";
    }


    my $store = $self->getStore();
    if(ref($store) eq 'ApiCommonModel::Model::JBrowseTrackConfig::RestStore') {
        my $query = $store->getQuery();
        my $baseUrl = $store->getBaseUrl();

        $jbrowseObject->{baseUrl}= $baseUrl;
        $jbrowseObject->{query} = {'feature' => $query };
        my $queryParams = $store->getQueryParamsHash();

        # add query param values
        if($queryParams) {
            foreach my $pk(keys %$queryParams) {
                $jbrowseObject->{query}->{$pk} = $queryParams->{$pk};
            }
        }
    }

    if(ref($store) eq 'ApiCommonModel::Model::JBrowseTrackConfig::GFFStore' ||
        ref($store) eq 'ApiCommonModel::Model::JBrowseTrackConfig::BigWigStore' ||
	ref($store) eq 'ApiCommonModel::Model::JBrowseTrackConfig::BedStore') {
	my $query = $store->getQuery();
        my $urlTemplate = $store->getUrlTemplate();
        $jbrowseObject->{urlTemplate}= $urlTemplate if($urlTemplate);
        $jbrowseObject->{query} = {'feature' => $query };
        my $queryParams = $store->getQueryParamsHash();
        
	# add query param values
        if($queryParams) {
            foreach my $pk(keys %$queryParams) {
                $jbrowseObject->{query}->{$pk} = $queryParams->{$pk};
            }
        }	

    }



    return $jbrowseObject;
}



sub getJBrowse2Object {
    my $self = shift;

    my $datasetConfig = $self->getDatasetConfigObj();

    my $datasetName = $datasetConfig->getDatasetName();
    my $studyDisplayName = $datasetConfig->getStudyDisplayName();
    my $summary = $datasetConfig->getSummary();

    my $metadata = $self->getMetadata();

    my $name = $self->getId();
    my $trackId = $self->getLabel();

    my $store = $self->getStore();
    my $storeType = $store->getStoreType();

    # JBrowse 2 does not work with REST Store
    if(ref($store) eq "ApiCommonModel::Model::JBrowseTrackConfig::RestStore") {
        return undef;
    }

    my $displayType = $self->getDisplayType();
    my $trackType = $self->getTrackType();

    #my $category = $datasetConfig->getCategory();
    my $subcategory = $datasetConfig->getSubcategory();


    my $id = $self->getId();
    my $label = $self->getLabel();
    my $category = $datasetConfig->getCategory() if ($datasetConfig);


    my @categoryHierarchy = ($category);
    if($subcategory) {
        push @categoryHierarchy, $subcategory;
        if($studyDisplayName) {
            push @categoryHierarchy, $studyDisplayName;
        }
    }

    unless($id || $name && $storeType && $trackId && $trackType && $category) {
        print Dumper $self;
        die "missing a required property (id, storeType, label, displayType, trackType, category)";
    }

    my $jbrowseObject = {type => $trackType,
                         trackId => $trackId,
                         name => $name,
                         category => \@categoryHierarchy,
                         adapter => { type => $storeType },
                         assemblyNames =>  [ $datasetConfig->getOrganismAbbrev() ],
                         displays => []

    };

    if($displayType) {
        $jbrowseObject->{displays}->[0] = {type => $displayType};
    }
    $jbrowseObject->{metadata} = $metadata if($metadata);

    return $jbrowseObject;



    die "Abstract method 'jbrowse2' must be implemented by subclass";
}

sub getApolloObject {
    my $self = shift;
    die "Abstract method 'apollo' must be implemented by subclass";
}

sub getConfigurationObject {
    my $self = shift;
    my $applicationType = $self->getApplicationType();

    if($applicationType eq 'jbrowse') {
        return $self->getJBrowseObject();
    }
    elsif($applicationType eq 'jbrowse2') {
        return $self->getJBrowse2Object();
    }

    elsif($applicationType eq 'apollo') {
        return $self->getApolloObject();
    }

    elsif($applicationType eq 'apollo3') {
        return $self->getApolloObject();
    }

    else {
        die "Application Type not recognized " . $applicationType;
    }
}


1;
