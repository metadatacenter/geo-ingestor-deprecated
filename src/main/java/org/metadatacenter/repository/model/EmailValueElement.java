package org.metadatacenter.repository.model;

import java.util.List;
import java.util.Optional;

public class EmailValueElement extends ValueElement
{
  public EmailValueElement(List<String> jsonLDTypes, Optional<String> jsonLDIdentifier, String value)
  {
    super(jsonLDTypes, jsonLDIdentifier, value);
  }
}