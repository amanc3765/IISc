# PAV2021 Project


## Soot package

The soot jar to use is present in the `pkgs` dir.

## Dependencies
Install Graphviz (optional)
Graphviz is used for generating the diagram.

```bash
sudo apt install graphviz
```

## Running the Analysis

After cloning the repo
```bash
cd <project-dir>
make
```


### Building each step explicitly

To recompile the targets (ie, the target classes, aka test inputs):
```bash
make build-targets
```


To recompile the Analysis:
```bash
make build-analysis
```


To run the analysis and generate the CFG diagram:
```bash
make run-analysis
```


## Adding your own test cases

Public test cases are kept in the "targets1-pub" directory.

You may add new test cases in  the "target2-mine" directory.


## Important code files

```bash
Analysis.java  -- main file. implement your analysis in this file
PAVBase.java -- auxiliary file. used to provide utility functions.
LatticeElement.java -- the LatticeElement interface definition
```

