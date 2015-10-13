package org.metadatacenter.model.investigation;

import org.metadatacenter.model.repository.MetadataTemplateElement;
import org.metadatacenter.model.repository.StringValueElement;

import java.util.List;
import java.util.Optional;

public class FactorValue  extends MetadataTemplateElement
{
  private final StringValueElement type;
  private final StringValueElement unit;
  private final StringValueElement value;
  private final Optional<StudyFactor> hasStudyFactor;

  public FactorValue(List<String> jsonLDTypes, Optional<String> jsonLDIdentifier, StringValueElement type,
      StringValueElement unit, StringValueElement value, Optional<StudyFactor> studyFactor)
  {
    super(jsonLDTypes, jsonLDIdentifier);
    this.type = type;
    this.unit = unit;
    this.value = value;
    this.hasStudyFactor = studyFactor;
  }
}