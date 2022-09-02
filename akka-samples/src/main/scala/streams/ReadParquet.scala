package streams
import akka.NotUsed
import akka.stream.alpakka.avroparquet.scaladsl.AvroParquetSource
import akka.stream.scaladsl.{Sink, Source}
import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.conf.Configuration
import org.apache.parquet.avro.{AvroParquetReader, AvroReadSupport}
import org.apache.parquet.hadoop.ParquetReader
import org.apache.parquet.hadoop.util.HadoopInputFile
import org.apache.hadoop.fs.Path
import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt



object ReadParquet extends App {
  implicit val actorSystem = ActorSystem("MaterializingStreams")
  import actorSystem.dispatcher


  val filePath = "src/main/resources/input/part-00000-f476f45e-e83b-40bb-b33f-e64932252771-c000.snappy.parquet"

  val conf: Configuration = new Configuration()
  conf.setBoolean(AvroReadSupport.AVRO_COMPATIBILITY, true)
  val reader: ParquetReader[GenericRecord] =
    AvroParquetReader.builder[GenericRecord](HadoopInputFile.fromPath(new Path(filePath), conf)).withConf(conf).build()

  val source: Source[GenericRecord, NotUsed] = AvroParquetSource(reader)

  //val consoleSink = Sink.foreach[GenericRecord](e=> println(e.get("Name")))
  val consoleSink = Sink.foreach[GenericRecord](println)

  source.runWith(consoleSink).andThen {
    case _ =>
      actorSystem.terminate()
      Await.ready(actorSystem.whenTerminated, 1 minute)
  }

}
