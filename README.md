# Pinecone Java Examples

The official repository for Pinecone examples written in Java.

For more information on the Java client, please refer to
the [documentation](https://docs.pinecone.io/reference/pinecone-clients#java-client).

## Documentation

All examples have individual READMEs. Please refer to the README in each example directory for more information
about the specific example.

## Installation

Each example has its own `pom.xml` file and is treated as its own, standalone Maven project.

## Configuration

At a minimum, all examples require a Pinecone API key. You can obtain an API key by signing up for a Pinecone account
at [pinecone.io](https://www.pinecone.io).

## Indexes

All examples use Pinecone [serverless indexes](https://docs.pinecone.io/reference/architecture/serverless-architecture).

## Contributing

We welcome contributions from the community! If you wish to contribute an example, please follow these guidelines:

- Ensure your example is well-documented and easy to understand.
- Include a README with instructions on how to run your example.
- Include a `pom.xml` file with all necessary dependencies.
- Make a directory for your example in the root of this repository, following the same layout as `semantic-search`.

PRs will be need to be reviewed and approved by the Pinecone team.