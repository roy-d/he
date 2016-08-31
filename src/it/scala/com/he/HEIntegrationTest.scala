package com.he

import io.finch.test.ServiceIntegrationSuite
import org.scalatest.Matchers
import org.scalatest.fixture.FlatSpec

class HEIntegrationTest extends FlatSpec with Matchers
  with ServiceIntegrationSuite with HEServiceSuite
