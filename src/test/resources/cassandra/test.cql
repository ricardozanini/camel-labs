/*
Used in test "com.stackoverflow.camel.labs.cassandra.KeyspaceUpperCaseTest"
*/

CREATE KEYSPACE TEST WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1} ;
CREATE TABLE test_table (name text PRIMARY KEY);