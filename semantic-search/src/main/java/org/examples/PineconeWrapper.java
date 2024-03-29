package org.examples;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.openapitools.client.model.*;

import java.util.List;

public class PineconeWrapper {
    private final String apiKey;
    private final String indexName;
    final Pinecone pinecone;

    public PineconeWrapper(String apiKey, String indexName) {
        this.apiKey = apiKey;
        this.indexName = indexName;
        this.pinecone = new Pinecone.Builder(this.apiKey).build();
    }

    private Index openPineconeIndexConnection() {
        return this.pinecone.createIndexConnection(this.indexName);
    }

    public void closePineconeIndexConnection() {
        this.pinecone.createIndexConnection(this.indexName).close();
    }

    public Boolean confirmIndexExists() {
        List<IndexModel> indexList = this.pinecone.listIndexes().getIndexes();
        if (indexList != null && indexList.stream().noneMatch(indexModel -> indexModel.getName().equals(indexName))) {
            return true;  // create index
        } else {
            return false;  // don't create index
        }
    }

    public CreateIndexRequestSpec setUpServerlessIndexSpec() {
        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AWS).region("us-west-2");
        return new CreateIndexRequestSpec().serverless(serverlessSpec);
    }

    // TODO: add tearDownServerlessIndex method
    public void buildServerlessIndex() {
        CreateIndexRequest indexBlueprint = new CreateIndexRequest()
                .name(this.indexName)
                .metric(IndexMetric.COSINE)
                .dimension(1536)
                .spec(this.setUpServerlessIndexSpec());
        this.pinecone.createIndex(indexBlueprint);
    }

    public Boolean indexFull(){
        try (Index index = this.openPineconeIndexConnection()) {
            int vectorCount;
            vectorCount = index.describeIndexStats(null).getTotalVectorCount();
            return vectorCount > 0;
        }
    }

    public void upsert(List<VectorWithUnsignedIndices> objs, String namespace) {
        try (Index index = this.openPineconeIndexConnection()) {
            index.upsert(objs, namespace);
        }
    }

    public QueryResponseWithUnsignedIndices query(int topK,
                                                  List<Float> vector,
                                                  List<Long> sparseIndices,
                                                  List<Float> sparseValues,
                                                  String id,
                                                  String namespace,
                                                  Struct filter,
                                                  boolean includeValues,
                                                  boolean includeMetadata) {
        try (Index index = this.openPineconeIndexConnection()) {
            return index.query(topK, vector, sparseIndices, sparseValues, id, namespace, filter, includeValues, includeMetadata);
        }
    }

    public Struct buildClaimFilter() {
        return Struct.newBuilder()
                .putFields("claimLabel", Value.newBuilder().setNumberValue(0).build())
                .build();
    }
}
