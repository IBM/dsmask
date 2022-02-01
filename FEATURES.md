# Current features

### F01. Static data masking for table-formatted data sets.

"Static" means that masking process is performed in a batch and
produces the copy of the source datasets with masking applied.

Note: Hierarchical (XML, JSON) data masking is possible through custom
scripts, although it requires significant efforts for any non-trivial
data structure.

### F02. Scalable high-performance masking process.

Based on IBM DataStage parallel job functionality. In most cases data
masking process can be scaled linearly by adding the additional
processing capacity.

### F03. Rule-based data masking.

Actual data masking operations are defined by assigning the proper
data class labels to the columns of the tables being processed.
Masking rules are defined for data classes, not for individual
columns.

### F04. Multi-value data masking.

Data masking rules can be applied to the combination of input values,
not just to a single input value. This enables more complex data
masking logic, like masking the person names based on gender.

The system automatically chooses the most complex rule available
based on the inputs and outputs defined, to better handle the
specific features of each data set.

### F05. Enforce uniqueness of masked data values.

Data masking rules can be defined with the requirement that the masked
values should be unique, which means that for each distinct input
the masking system should generate a distinct (non-repeated) output.

This function is supported by storing the pairs of source and masked
values in a key-value internal database. When the collision is detected,
the masking system re-generates the masked value for the same input,
but with different parameters, and repeats the check with new output.
If the collision cannot be avoided in a defined number of iterations,
the masking process reports an error.

In many cases this allows to use algorithms like FPE without the risk
of generating duplicate values, which are not acceptable, for example,
as payment card numbers.

### F06. Groovy language for custom masking logic.

This sample uses Groovy (instead of Lua, which is used in IBM Optim)
to develop the custom masking logic.

Groovy is easier for most modern data engineers due to its similarity
with Java and better/simpler Unicode string support.

### F07. Single masking job design for any input table structure.

One DataStage job design can handle the masking of all tables
in the particular data source type. Less efforts for data masking
implementation.

### F08. Masking the whole database at once.

Includes the example implementation of a script to automatically
run all the jobs which need to be executed to mask the data in
the chosen source database.

The example implementation identifies the tables which contain
the fields to be masked, and for each such table starts a separate
masking job. It also includes a safeguard against running multiple
concurrent masking jobs "packs" over a single source database.

Sample reports has been prepared to show the results of the data
masking job "pack" executed on the particular source database.


# Features yet to be designed and implemented

### F30. Support for rule-based masking of hierarchical data.

Additional functionality is needed to support masking of hierarchical
data based on format definition, data classes and masking rules.

### F31. Graphical editor of data masking rules.

Web browser based. Built-in masking rule debugger is needed,
showing the results of masking the data samples updated according
to the current state of the masking rule being edited.

### F32. Extension API for data masking algorithms.

The current implementation has a fixed set of built-in data masking
algorithms supported.

A better architecture would enable the data masking algorithms
to be added as plug-ins, in generally the same way as the current
support for custom scripts.

### F33. Web console for data masking processes.

A single web browser based console to request and monitor all
data masking activities.
