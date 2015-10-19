package org.metadatacenter.ingestors.geo;

import org.metadatacenter.ingestors.geo.metadata.ContributorName;
import org.metadatacenter.ingestors.geo.metadata.GEOMetadata;
import org.metadatacenter.ingestors.geo.metadata.Platform;
import org.metadatacenter.ingestors.geo.metadata.Protocol;
import org.metadatacenter.ingestors.geo.metadata.Sample;
import org.metadatacenter.ingestors.geo.metadata.Series;
import org.metadatacenter.models.investigation.Characteristic;
import org.metadatacenter.models.investigation.CharacteristicValue;
import org.metadatacenter.models.investigation.Contact;
import org.metadatacenter.models.investigation.DataFile;
import org.metadatacenter.models.investigation.Input;
import org.metadatacenter.models.investigation.Investigation;
import org.metadatacenter.models.investigation.Output;
import org.metadatacenter.models.investigation.ParameterValue;
import org.metadatacenter.models.investigation.Process;
import org.metadatacenter.models.investigation.ProtocolParameter;
import org.metadatacenter.models.investigation.Publication;
import org.metadatacenter.models.investigation.Study;
import org.metadatacenter.models.investigation.StudyAssay;
import org.metadatacenter.models.investigation.StudyFactor;
import org.metadatacenter.models.investigation.StudyGroupPopulation;
import org.metadatacenter.models.investigation.StudyProtocol;
import org.metadatacenter.models.investigation.StudyTime;
import org.metadatacenter.repository.model.DateValueElement;
import org.metadatacenter.repository.model.StringValueElement;
import org.metadatacenter.repository.model.URIValueElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.metadatacenter.repository.model.RepositoryFactory.createOptionalStringValueElement;
import static org.metadatacenter.repository.model.RepositoryFactory.createStringValueElement;

/**
 * Take a {@link GEOMetadata} object and convert it to a CEDAR {@link Investigation} object.
 *
 * @see GEOMetadata
 * @see Investigation
 */
public class GEOMetadata2InvestigationConverter
{
  private static final String INVESTIGATION_TEMPLATE_ID = "Investigation";

  // TODO GEO Series variables and repeat fields
  public Investigation convertGeoMetadata2Investigation(GEOMetadata geoMetadata)
  {
    Series geoSeries = geoMetadata.getSeries();

    String templateID = INVESTIGATION_TEMPLATE_ID;
    StringValueElement title = createStringValueElement(geoSeries.getTitle());
    StringValueElement description = createStringValueElement(concatenateFieldValues(geoSeries.getSummary()));
    StringValueElement identifier = createStringValueElement(geoSeries.getTitle());
    Optional<DateValueElement> submissionDate = Optional.empty();
    Optional<DateValueElement> publicReleaseDate = Optional.empty();
    Optional<StudyProtocol> studyProtocol = convertGEOProtocol2StudyProtocol(geoMetadata.getProtocol());
    Study study = convertGEOSeries2Study(geoMetadata.getSeries(), geoMetadata.getSamples(), geoMetadata.getPlatform(),
      studyProtocol);

    return new Investigation(templateID, title, description, identifier, submissionDate, publicReleaseDate,
      Collections.singletonList(study));

  }

  private Study convertGEOSeries2Study(Series geoSeries, Map<String, Sample> geoSamples, Optional<Platform> geoPlatform,
    Optional<StudyProtocol> studyProtocol)
  {
    StringValueElement title = createStringValueElement(geoSeries.getTitle());
    StringValueElement description = createStringValueElement(concatenateFieldValues(geoSeries.getSummary()));
    StringValueElement identifier = createStringValueElement(geoSeries.getTitle());
    Optional<DateValueElement> submissionDate = Optional.empty();
    Optional<DateValueElement> publicReleaseDate = Optional.empty();
    Optional<URIValueElement> studyDesignType = Optional.empty();
    Optional<StudyAssay> studyAssay = convertGEOPlatform2StudyAssay(geoPlatform);
    List<Process> processes = convertGEOSamples2Processes(geoSamples, studyAssay, studyProtocol);
    List<StudyFactor> studyFactors = new ArrayList<>(); // TODO Not clear where these are in GEO.
    Optional<StudyGroupPopulation> studyGroupPopulation = Optional.empty(); // Not recorded in GEO
    List<Publication> publications = convertPubmedIDs2Publications(geoSeries.getPubmedIDs());
    List<Contact> contacts = convertGEOContributors2Contacts(geoSeries.getContributors());
    List<StudyAssay> studyAssays = studyAssay.isPresent() ?
      Collections.singletonList(studyAssay.get()) :
      Collections.emptyList();

    return new Study(title, description, identifier, submissionDate, publicReleaseDate, studyDesignType, processes,
      studyProtocol, studyAssays, studyFactors, studyGroupPopulation, publications, contacts);
  }

  private List<Process> convertGEOSamples2Processes(Map<String, Sample> geoSamples, Optional<StudyAssay> studyAssay,
    Optional<StudyProtocol> studyProtocol)
  {
    List<Process> processes = new ArrayList<>();

    for (String sampleName : geoSamples.keySet()) {
      Process process = convertGEOSample2Process(geoSamples.get(sampleName), studyAssay, studyProtocol);
      processes.add(process);
    }
    return processes;
  }

  private Process convertGEOSample2Process(Sample geoSample, Optional<StudyAssay> studyAssay,
    Optional<StudyProtocol> studyProtocol)
  {
    StringValueElement type = createStringValueElement("GEOSampleProcess");
    List<ParameterValue> hasParameterValue = new ArrayList<>(); // Stored via the study protocol
    Optional<StudyAssay> sampleStudyAssay = extractStudyAssayFromGEOSample(geoSample);
    org.metadatacenter.models.investigation.Sample sample = extractSampleFromGEOSample(geoSample);
    List<DataFile> dataFiles = extractDataFilesFromGEOSample(geoSample);
    List<Input> hasInput = new ArrayList<>();
    List<Output> hasOutput = new ArrayList<>();

    hasInput.add(sample);
    hasOutput.addAll(dataFiles);

    return new Process(type, studyAssay.isPresent() ? studyAssay : sampleStudyAssay, studyProtocol, hasParameterValue,
      hasInput, hasOutput);
  }

  private org.metadatacenter.models.investigation.Sample extractSampleFromGEOSample(
    org.metadatacenter.ingestors.geo.metadata.Sample geoSample)
  {
    StringValueElement name = createStringValueElement(geoSample.getSampleName());
    StringValueElement type = createStringValueElement(geoSample.getPlatform());
    Optional<StringValueElement> description = createOptionalStringValueElement(geoSample.getTitle());
    Optional<StringValueElement> source = createOptionalStringValueElement(geoSample.getBiomaterialProvider());
    List<Characteristic> characteristics = convertGEOCharacteristics2Characteristics(geoSample.getCharacteristics());
    Optional<StudyTime> hasStudyTime = Optional.empty(); // Not present

    return new org.metadatacenter.models.investigation.Sample(name, type, description, source, characteristics,
      hasStudyTime);
  }

  private Optional<StudyAssay> extractStudyAssayFromGEOSample(Sample geoSample)
  {
    return Optional.of(new StudyAssay(createStringValueElement(geoSample.getPlatform())));
  }

  private Optional<StudyAssay> convertGEOPlatform2StudyAssay(Optional<Platform> geoPlatform)
  {
    if (geoPlatform.isPresent())
      return Optional.of(new StudyAssay(createStringValueElement(geoPlatform.get().getTitle()),
        createOptionalStringValueElement(Optional.of(geoPlatform.get().getDistribution())),
        createOptionalStringValueElement(Optional.of(geoPlatform.get().getTechnology()))));
    else
      return Optional.empty();
  }

  private Optional<StudyProtocol> convertGEOProtocol2StudyProtocol(Protocol geoProtocol)
  {
    StringValueElement name = createStringValueElement(
      geoProtocol.getLabel().isPresent() ? geoProtocol.getLabel().get() : "");
    StringValueElement description = createStringValueElement(
      geoProtocol.getValueDefinition().isPresent() ? geoProtocol.getValueDefinition().get() : "");

    Optional<StringValueElement> type = Optional.empty();
    Optional<URIValueElement> uri = Optional.empty();
    Optional<StringValueElement> version = Optional.empty();
    List<ProtocolParameter> protocolParameters = new ArrayList<>();

    if (geoProtocol.getGrowth().isPresent())
      protocolParameters.add(createProtocolParameter("growth", geoProtocol.getGrowth().get()));

    if (geoProtocol.getTreatment().isPresent())
      protocolParameters.add(createProtocolParameter("treatment", geoProtocol.getTreatment().get()));

    if (geoProtocol.getExtract().isPresent())
      protocolParameters.add(createProtocolParameter("extract", geoProtocol.getExtract().get()));

    if (geoProtocol.getLabel().isPresent())
      protocolParameters.add(createProtocolParameter("label", geoProtocol.getLabel().get()));

    if (geoProtocol.getHyb().isPresent())
      protocolParameters.add(createProtocolParameter("hyb", geoProtocol.getHyb().get()));

    if (geoProtocol.getScan().isPresent())
      protocolParameters.add(createProtocolParameter("scan", geoProtocol.getScan().get()));

    if (geoProtocol.getDataProcessing().isPresent())
      protocolParameters.add(createProtocolParameter("dataProcessing", geoProtocol.getDataProcessing().get()));

    if (geoProtocol.getValueDefinition().isPresent())
      protocolParameters.add(createProtocolParameter("valueDefinition", geoProtocol.getValueDefinition().get()));

    return Optional.of(new StudyProtocol(name, description, type, uri, version, protocolParameters));
  }

  private List<Characteristic> convertGEOCharacteristics2Characteristics(Map<String, String> geoCharacteristics)
  {
    List<Characteristic> characteristics = new ArrayList<>();

    for (String geoCharacteristicName : geoCharacteristics.keySet()) {
      String geoCharacteristicValue = geoCharacteristics.get(geoCharacteristicName);
      CharacteristicValue characteristicValue = new CharacteristicValue(
        createStringValueElement(geoCharacteristicValue));
      Characteristic characteristic = new Characteristic(createStringValueElement(geoCharacteristicName),
        Optional.of(characteristicValue));

      characteristics.add(characteristic);
    }

    return characteristics;
  }

  private ProtocolParameter createProtocolParameter(String parameterName, String value)
  {
    return createProtocolParameter(parameterName, Optional.empty(), value, Optional.empty(), Optional.empty());
  }

  private ProtocolParameter createProtocolParameter(String parameterName, Optional<String> parameterDescription,
    String value, Optional<String> type, Optional<String> unit)
  {
    ParameterValue parameterValue = new ParameterValue(createStringValueElement(value),
      createOptionalStringValueElement(type), createOptionalStringValueElement(unit));

    return new ProtocolParameter(createStringValueElement(parameterName),
      createOptionalStringValueElement(parameterDescription), Optional.of(parameterValue));
  }

  private List<Contact> convertGEOContributors2Contacts(List<ContributorName> geoContributors)
  {
    List<Contact> contacts = new ArrayList<>();

    for (ContributorName contributorName : geoContributors) {
      Contact contact = new Contact(createStringValueElement(contributorName.getFirstName()),
        createStringValueElement(contributorName.getMiddleInitial()),
        createStringValueElement(contributorName.getLastName()));

      contacts.add(contact);
    }
    return contacts;
  }

  private List<DataFile> extractDataFilesFromGEOSample(org.metadatacenter.ingestors.geo.metadata.Sample geoSample)
  {
    List<DataFile> dataFiles = new ArrayList<>();

    for (String rawDataFile : geoSample.getRawDataFiles()) {
      DataFile dataFile = new DataFile(createStringValueElement(rawDataFile),
        Optional.of(new StringValueElement("raw")));
      dataFiles.add(dataFile);
    }

    if (geoSample.getCELFile().isPresent()) {
      DataFile dataFile = new DataFile(createStringValueElement(geoSample.getCELFile().get()),
        Optional.of(new StringValueElement("cel")));
      dataFiles.add(dataFile);
    }

    if (geoSample.getEXPFile().isPresent()) {
      DataFile dataFile = new DataFile(createStringValueElement(geoSample.getEXPFile().get()),
        Optional.of(new StringValueElement("exp")));
      dataFiles.add(dataFile);
    }

    if (geoSample.getCHPFile().isPresent()) {
      DataFile dataFile = new DataFile(createStringValueElement(geoSample.getCHPFile().get()),
        Optional.of(new StringValueElement("chp")));
      dataFiles.add(dataFile);
    }
    return dataFiles;
  }

  private List<Publication> convertPubmedIDs2Publications(List<String> pubmedIDs)
  {
    List<Publication> publications = new ArrayList<>();

    for (String pubmedID : pubmedIDs) {
      StringValueElement pubmedIDValueElement = createStringValueElement(pubmedID);
      Publication publication = new Publication(pubmedIDValueElement);
      publications.add(publication);
    }
    return publications;
  }

  private String concatenateFieldValues(List<String> fieldValues)
  {
    StringBuilder sb = new StringBuilder();
    boolean isFirst = true;
    for (String fieldValue : fieldValues) {
      if (!isFirst)
        sb.append(GEONames.MULTI_VALUE_FIELD_SEPARATOR);
      sb.append(fieldValue);
      isFirst = false;
    }
    return sb.toString();
  }
}
