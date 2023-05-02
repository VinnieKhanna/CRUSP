package nt.tuwien.ac.at.controller;

class TestData {
    static final String requestJsonStandard1 = "{" +
            "\"repeats\":1," +
            "\"volume\":58," +
            "\"packetSize\":6200," +
            "\"rate\":150," +
            "\"sleep\":200," +
            "\"timeout\":100," +
            "\"standard\":true" +
            "}";

    static final String requestJsonStandard2 = "{" +
            "\"repeats\":3," +
            "\"volume\":58," +
            "\"packetSize\":6200," +
            "\"rate\":150," +
            "\"sleep\":200," +
            "\"timeout\":100," +
            "\"standard\":true" +
            "}";

    static final String requestJsonNotStandard = "{" +
            "\"repeats\":2," +
            "\"volume\":58," +
            "\"packetSize\":6200," +
            "\"rate\":150," +
            "\"sleep\":200," +
            "\"timeout\":100," +
            "\"standard\":false" +
            "}";

    //this is the same as requestJsonStandard1
    static final String requestStandardIsNull = "{" +
            "\"repeats\":1," +
            "\"volume\":58," +
            "\"packetSize\":6200," +
            "\"rate\":150," +
            "\"sleep\":200," +
            "\"timeout\":100," +
            "\"standard\":null" + // this is interpreted as standard = false
            "}";

    static final String requestPacketSizeIs0 = "{" +
            "\"repeats\":2," +
            "\"volume\":58," +
            "\"packetSize\":0," +
            "\"rate\":150," +
            "\"sleep\":200," +
            "\"timeout\":100," +
            "\"standard\":false" +
            "}";

    static final String requestFilter = "{\n" +
            "  \"sorted\": [\n" +
            "    {\n" +
            "      \"id\": \"repeats\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"volume\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"packetSize\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"rate\",\n" +
            "      \"desc\": true\n" +
            "    }\n" +
            "  ],\n" +
            "  \"filtered\": [\n" +
            "    {\n" +
            "      \"id\": \"repeats\",\n" +
            "      \"value\": \"1\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"volume\",\n" +
            "      \"value\": \"58\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"packetSize\",\n" +
            "      \"value\": \"6200\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"rate\",\n" +
            "      \"value\": \"150\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    static final String requestFilter2 = "{\n" +
            "  \"sorted\": [\n" +
            "    {\n" +
            "      \"id\": \"repeats\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"volume\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"packetSize\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"rate\",\n" +
            "      \"desc\": true\n" +
            "    }\n" +
            "  ],\n" +
            "  \"filtered\": [\n" +
            "    {\n" +
            "      \"id\": \"repeats\",\n" +
            "      \"value\": \"1-2\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"volume\",\n" +
            "      \"value\": \"58\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"packetSize\",\n" +
            "      \"value\": \"6200\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"rate\",\n" +
            "      \"value\": \"150\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}

