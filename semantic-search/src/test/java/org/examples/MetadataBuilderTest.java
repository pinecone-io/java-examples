package org.examples;

import com.google.protobuf.Struct;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;

// TODO: should I pull null and empty checks here, or in SemanticSearchHandler, since this is a helper class?
public class MetadataBuilderTest {
    @Test
    public void testBuildMetadataStruct() {
        Struct toyData = MetadataBuilder.build("Test claim text", "Test claim ID", 1, new JSONArray("[\"Test article" +
                " 1\", " +
                "\"Test article 2\"]"));
        assert toyData.getFieldsMap().get("claimText").getStringValue().equals("Test claim text");
    }
}
