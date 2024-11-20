#!/usr/bin/perl

use strict;

use lib "$ENV{GUS_HOME}/lib/perl";

use DBI;
use DBD::Oracle;

use CBIL::Bio::SequenceUtils; #reverseComplementSequence

use Getopt::Long qw(GetOptions);

use Data::Dumper;

use EbrcModelCommon::Model::tmUtils;

my ($help, $propfile, $instance, $schema, $suffix, $prefix, $filterValue, $debug);

Getopt::Long::Configure("pass_through");
GetOptions("propfile=s" => \$propfile,
           "instance=s" => \$instance,
           "schema=s" => \$schema,
           "suffix=s" => \$suffix,
           "prefix=s" => \$prefix,
           "filterValue=s" => \$filterValue,
           "debug!" => \$debug,
           "help|h" => \$help,
          );

die "required parameter missing" unless ($propfile && $instance && $suffix);

my $dbh = EbrcModelCommon::Model::tmUtils::getDbHandle($instance, $schema, $propfile);

my $insertSql = "INSERT INTO ${schema}.TranscriptGenomicSequence${suffix} 
    (gene_source_id, source_id, project_id,
     genomic_sequence, genomic_sequence_length)
 VALUES (?,?,?,?,?)";
my $insertSh = $dbh->prepare($insertSql);

my $sql = "SELECT ga.source_id AS gene_source_id
     , ga.project_id AS project_id
     , t.source_id
     , tl.start_min
     , tl.end_max
     , tl.is_reversed
     , gss.source_id
     , gss.sequence
FROM apidb.transcriptlocation tl
   , apidbtuning.geneattributes ga
   , dots.transcript t
   , apidbtuning.genomicsequencesequence gss
WHERE t.na_feature_id  = tl.na_feature_id
AND t.parent_id = ga.na_feature_id
AND tl.sequence_source_id = gss.source_id
ORDER BY gss.source_id";


# get the lob locator object here
my $sh = $dbh->prepare($sql, { ora_auto_lob => 0 } )
     or die "Can't prepare SQL statement: " . $dbh->errstr(); 
$sh->execute();

my ($prevSequenceSourceId, $sequence, $count);
while(my ($geneSourceId, $projectId, $sourceId, $start, $end, $isReversed, $sequenceSourceId, $sequenceLobLocator) = $sh->fetchrow_array()) {

  if($prevSequenceSourceId ne $sequenceSourceId) {
    print STDERR "Processing Transcripts for sequence $sequenceSourceId\n";
    $sequence = &readClob($dbh, $sequenceLobLocator);
  }

  my $substrStart = $start - 1;
  my $substrLength = $end - $start + 1;
  
  my $transcriptGenomicSequence = substr($sequence, $substrStart, $substrLength);

  if($isReversed) {
    $transcriptGenomicSequence = CBIL::Bio::SequenceUtils::reverseComplementSequence($transcriptGenomicSequence);
  }

  $insertSh->execute($geneSourceId, $sourceId, $projectId, $transcriptGenomicSequence, $substrLength);

  if($count++ % 2000 == 0) {
    print STDERR "Commit point\n";
    $dbh->commit();
  }

  $prevSequenceSourceId = $sequenceSourceId;
}

$dbh->commit();
$dbh->disconnect();


sub readClob {
  my ($dbh, $lobLocator) = @_;

  my $chunkSize = $dbh->ora_lob_chunk_size($lobLocator);
  my $offset = 1;   # Offsets start at 1, not 0
  
  my $output;
  while(1) {
    my $data = $dbh->ora_lob_read($lobLocator, $offset, $chunkSize );
    last unless length $data;
    $output .= $data;
    $offset += $chunkSize;
  }
  return $output;
}                 


1;

