package org.examples;

import com.google.protobuf.Struct;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

// TODO: should I put null and empty checks here, or in SemanticSearchHandler, since this is a helper class?
public class MetadataBuilderTest {
    @Test
    public void testBuildMetadataStruct() {
        Struct toyData = MetadataBuilder.build("Test claim text", "Test claim ID", 1, new JSONArray("[\"Test article" +
                " 1\", " +
                "\"Test article 2\"]"));
        assert toyData.getFieldsMap().get("claimText").getStringValue().equals("Test claim text");
    }

    @Test
    public void testMetadataItemsNotNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            Struct toyData = MetadataBuilder.build(null, "Test claim ID", 1, new JSONArray("[\"Test article" +
                    " 1\", " +
                    "\"Test article 2\"]"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Struct toyData = MetadataBuilder.build("Test claim text", null, 1, new JSONArray("[\"Test article" +
                    " 1\", " +
                    "\"Test article 2\"]"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Struct toyData = MetadataBuilder.build("Test claim text", "Test claim ID", null, new JSONArray("[\"Test " +
                    "article" +
                    " 1\", " +
                    "\"Test article 2\"]"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Struct toyData = MetadataBuilder.build("Test claim text",
                    "Test claim ID",
                    1,
                    new JSONArray("[]"));
        });
    }

}
