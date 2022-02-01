# DsMask Scalable Data Masking Sample Code

This repository contains the sample code, which shows a way to implement complex
policy-based data masking on IBM DataStage platform, using masking
algorithms coming with IBM InfoSphere Optim.

This sample code shows how to solve the problem of high-performance scalable
static data masking based on masking rules, defining the masking operations
which should be applied to the specific types of confidential information.
The sample code can be used as a basis to build the actual data masking system
using the IBM DataStage and IBM Optim.

This sample code also contains the example setup for data masking adjusted
for the typical requirements of customers in Russian Federation.

The sample code provided in this repository has been iteratively developed
and improved by pre-sales specialists of IBM EE/A as part of multiple
pilot and demo implementation, to address the various requirements coming
from the customers.


# High-level logical overview

Components of IBM Information Server used:
- Information Governance Catalog (IGC), a metadata management tool;
- Information Analyzer (IA), a data profiling tool;
- DataStage, a ETL tool.

![DsMask Architecture](https://github.ibm.com/MZinal/dsmask-publish/blob/master/docs/dsmask-solution-schema.png)

The actual data masking uses the algorithms of IBM InfoSphere Optim
Data Privacy Providers Library (ODPP), through the Java API.
This leads to a technical limitation that the solution can only
run on Windows and Linux x86-64 platforms, because ODPP Java API
is not supported on AIX.

The types of confidential information are defined through the data classes.
Those data classes are defined in the Information Governance Catalog (IGC),
along with the table structure definitions.

Data classes are assigned to the columns of tables manually in IGC,
or in an automated way through the IA.

On top of the data classes, the data masking engineer/developer
prepares a set of data masking rules in the special XML-based format,
which link the actual data classes to the masking operations which
need to be performed.

Each masking operation is defined as a sequence of steps, which
needs to be applied to the input values to provide the (masked)
output values. Each step calls some masking or data preparation
algorithm, and can use the outputs of the previous steps as its
input data.

The masking rules are linked to the actual table's fields in
accordance to the data classess assigned in IGC. The actual set
if masking operations to be performed on the particular table
is calculated by the "configuration program" and is stored
in the internal configuration database as an object called
"masking profile".

Masking is performed by the custom Java-based DataStage operator,
which reads the "masking profile" and applies it to the input data,
providing the output data. The operator ensures that the input
and output values are different, and generates warnings otherwise.

Flexible DataStage job design is used, based on the RCP (Runtime
Column Propagation) feature and job parameters, and allows to handle
masking of all tables from the particular data source type (e.g. Oracle, 
or Db2, or MSSQL) with just a single job design.

# Custom components included

`ia-custom-ru` - set of data class definitions for the Russian market,
with the customized logic for IA scanning.

`dsmask-algo` and `dsmask-beans` - the supporting libraries to handle
data preparation and normalization, including some types of text values
pre-processing which is hard to implement using "plain" ODPP.

`dsmask-mock` - the library of algorithms to generate synthetic data
used by the JUnit tests. Only used when running the tests, not included
in the target binaries.

`dsmask-uniq` - a network service which implements the global uniqueness
checks of the masked values (e.g. ensuring that no two distinct input values
will be mapped to a single masked value), by storing the mapping between
the input and masked values. This service is optionally used by
the data masking rules (if enabled).

`dsmask-jconf` - the configuration program, which reads the masking rules,
loads the mapping between the table fields and confidential data classes,
and writes the "masking profiles" to the configuration database. It also
includes the logic to build the substitution dictionary for the names
of people in Russian language.

`dsmask-jmask` - the custom Java-based data masking operator for DataStage.

`dsjob` - sample job designs for masking and substitution dictionary
generation.

`reports` - sample reports on data masking activities recorded
in the DataStage job logs (stored in DSODB database) using the Pentaho
report generator.

`batcher` - the example script for running the masking DataStage jobs over
the set of tables, and wait for the result.

`dict-data` - sample dictionaries for masking of data on the Russian market.

`rules-testsuite` - sample data masking rules, used in the internal tests
for the configuration program and for the data masking operator.
