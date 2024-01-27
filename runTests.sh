mvn clean package
java --enable-preview -cp target/serde.jar org.example.FMASerDe
java --enable-preview -cp target/serde.jar org.example.FMASerDeOffHeap
