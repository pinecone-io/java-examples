package org.examples;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.*;

import java.util.List;

public class PineconeManager {
    private final String apiKey;
    private final String indexName;

    public PineconeManager(String apiKey, String indexName) {
        this.apiKey = apiKey;
        this.indexName = indexName;
    }

    public Pinecone connectToPineconeSvc() {
        return new Pinecone(this.apiKey);
    }

    public Index connectToPineconeIndex() {
        return this.connectToPineconeSvc().createIndexConnection(this.indexName);
    }

    public void closePineconeIndex(Index pineconeIndex) {
        pineconeIndex.close();
    }

    public Boolean confirmIndexExists(Pinecone pineconeSvc) {
        List<IndexModel> indexList = pineconeSvc.listIndexes().getIndexes();
        if (indexList != null && indexList.stream().noneMatch(indexModel -> indexModel.getName().equals(indexName))) {
            return true;  // create index
        } else {
            return false;  // don't create index
        }
    }

    public CreateIndexRequestSpec setUpServerlessIndex() {
        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AWS).region("us-west-2");
        return new CreateIndexRequestSpec().serverless(serverlessSpec);
    }

    public void buildIndex() {
        CreateIndexRequest indexBlueprint = new CreateIndexRequest()
                .name(this.indexName)
                .metric(IndexMetric.COSINE)
                .dimension(1536)
                .spec(this.setUpServerlessIndex());
        this.connectToPineconeSvc().createIndex(indexBlueprint);
    }
}
