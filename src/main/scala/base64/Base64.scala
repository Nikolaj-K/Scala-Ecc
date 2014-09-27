package base64
import scala.annotation.tailrec

/**
 * Base64 encoder
 * @author Mark Lister
 * (c) Mark Lister 2014
 *
 * I, the copyright holder of this work, release this work into the public domain.
 * This applies worldwide. In some countries this may not be legally possible;
 * if so: I grant anyone the right to use this work for any purpose, without any
 * conditions, unless such conditions are required by law.
 * 
 * The repo for this Base64 encoder lives at  https://github.com/marklister/base64
 * Please send your issues, suggestions and pull requests there.
 */

object Base64 {
  private[this] val encodeTable: IndexedSeq[Char] = ('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9') ++ Seq('+', '/')
  private[this] val decodeMap=collection.immutable.TreeMap(encodeTable.zipWithIndex : _*)
  private[this] val basic = Array(127, 127, 127).map(_.toByte) //to force only positive BigInts
  private[this] val zero = Array(0, 0).map(_.toByte)

  implicit class Encoder(b: Array[Byte]) {
    def toBase64: String = {
      @tailrec
      def enc(z: BigInt,acc:Seq[Char]): Seq[Char] =
        if (z == 0) acc
        else  enc(z / 64,encodeTable((z % 64).toInt)+:acc)

      val pad = (3 - b.length % 3) % 3
      val bi = BigInt(basic ++ b ++ zero.take(pad))
      
      (enc(bi,Seq.empty).drop(4).dropRight(pad) :+ "=" * pad).mkString
    }
  }
  
  implicit class Decoder(s: String) {
    lazy val cleanS = s.reverse.dropWhile(_ == '=').reverse
    lazy val pad = s.length - cleanS.length

    def toByteArray: Array[Byte] = {
      if (pad > 2 || s.length % 4 != 0) throw new java.lang.IllegalArgumentException("Invalid Base64 String:" + s)
      if (!cleanS.forall(encodeTable.contains(_))) throw new java.lang.IllegalArgumentException("Invalid Base64 String:" + s)

      (cleanS + "A" * pad)
        .foldLeft(BigInt(127))((a, b) => a * 64 + decodeMap(b))
        .toByteArray
        .drop(1).dropRight(pad)
    }
  }
}
