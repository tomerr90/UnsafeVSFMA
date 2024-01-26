mvn clean package
java --enable-preview -cp target/serde.jar org.example.FMASerDe > fmaSerDe
java --enable-preview -cp target/serde.jar org.example.FMASerDeOffHeap > fmaSerDeOffHeap
