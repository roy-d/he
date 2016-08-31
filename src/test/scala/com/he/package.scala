package com

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

package object he {

  implicit val queryArbitrary: Arbitrary[Query] = Arbitrary(
    for {
      param1 <- Gen.alphaStr
      param2 <- Gen.alphaStr
    } yield Query(param1, Some(param2))
  )
}
