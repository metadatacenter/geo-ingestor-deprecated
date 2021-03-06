package org.metadatacenter.models.investigation;

import org.metadatacenter.repository.model.DateTemplateFieldInstance;
import org.metadatacenter.repository.model.MetadataTemplateInstance;
import org.metadatacenter.repository.model.Namespaces;
import org.metadatacenter.repository.model.StringTemplateFieldInstance;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Investigation extends MetadataTemplateInstance
{
  public static final List<String> ElementURIs = Collections
    .singletonList(Namespaces.TEMPLATE_URI_BASE + "Investigation");

  private final StringTemplateFieldInstance title;
  private final StringTemplateFieldInstance description;
  private final StringTemplateFieldInstance identifier;
  private final Optional<DateTemplateFieldInstance> submissionDate;
  private final Optional<DateTemplateFieldInstance> publicReleaseDate;
  private final List<Study> study;

  public Investigation(List<String> jsonLDTypes, Optional<String> jsonLDIdentifier, String templateID,
    StringTemplateFieldInstance title, StringTemplateFieldInstance description, StringTemplateFieldInstance identifier,
    Optional<DateTemplateFieldInstance> submissionDate, Optional<DateTemplateFieldInstance> publicReleaseDate, List<Study> studies)
  {
    super(jsonLDTypes, jsonLDIdentifier, templateID);
    this.title = title;
    this.description = description;
    this.identifier = identifier;
    this.submissionDate = submissionDate;
    this.publicReleaseDate = publicReleaseDate;
    this.study = Collections.unmodifiableList(studies);
  }

  public Investigation(String templateID, StringTemplateFieldInstance title, StringTemplateFieldInstance description,
    StringTemplateFieldInstance identifier, Optional<DateTemplateFieldInstance> submissionDate,
    Optional<DateTemplateFieldInstance> publicReleaseDate, List<Study> studies)
  {
    super(ElementURIs, generateJSONLDIdentifier(Namespaces.TEMPLATE_INSTANCES_URI_BASE), templateID);
    this.title = title;
    this.description = description;
    this.identifier = identifier;
    this.submissionDate = submissionDate;
    this.publicReleaseDate = publicReleaseDate;
    this.study = Collections.unmodifiableList(studies);
  }
}
