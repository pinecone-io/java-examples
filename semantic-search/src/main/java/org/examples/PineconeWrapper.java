package org.examples;

import io.pinecone.clients.Index;
import org.openapitools.client.model.*;

import java.util.List;

public class PineconeWrapper {
    private final String apiKey;
    private final String indexName;

    public PineconeWrapper(String apiKey, String indexName) {
        this.apiKey = apiKey;
        this.indexName = indexName;
    }

    // TODO: add closePineconeConnection method
    public io.pinecone.clients.Pinecone openPineconeConnection() {
        return new io.pinecone.clients.Pinecone(this.apiKey);
    }

    public Index openPineconeIndexConnection() {
        return this.openPineconeConnection().createIndexConnection(this.indexName);
    }

    public void closePineconeIndexConnection(Index pineconeIndex) {
        pineconeIndex.close();
    }

    public Boolean confirmIndexExists(io.pinecone.clients.Pinecone pineconeSvc) {
        List<IndexModel> indexList = pineconeSvc.listIndexes().getIndexes();
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
        this.openPineconeConnection().createIndex(indexBlueprint);
    }
}
