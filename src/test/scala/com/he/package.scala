package com

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

package object he {

  implicit val queryArbitrary: Arbitrary[HEQuery] = Arbitrary(
    for {
      param1 <- Gen.alphaStr
      param2 <- Gen.alphaStr
    } yield HEQuery(param1, Some(param2))
  )
}
