package org.metadatacenter.ingestors.geo.metadata;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Metadata for a GEO series.
 *
 * @see GEOMetadata
 */
public class Series
{
  private final String title;
  private final List<String> summary;
  private final List<String> overallDesign;
  private final List<ContributorName> contributors;
  private final List<String> pubmedIDs;
  private final Map<String, Map<String, String>> variables; // sample name -> (variable name -> value)
  private final Map<String, List<String>> repeat; // sample name -> [repeat type]

  public Series(String title, List<String> summary, List<String> overallDesign, List<ContributorName> contributors,
    List<String> pubmedIDs, Map<String, Map<String, String>> variables, Map<String, List<String>> repeat)
  {
    this.title = title;
    this.summary = summary;
    this.overallDesign = overallDesign;
    this.contributors = contributors;
    this.pubmedIDs = pubmedIDs;
    this.variables = variables;
    this.repeat = repeat;
  }

  public String getTitle()
  {
    return title;
  }

  public List<String> getSummary()
  {
    return summary;
  }

  public List<String> getOverallDesign()
  {
    return overallDesign;
  }

  public List<ContributorName> getContributors()
  {
    return Collections.unmodifiableList(contributors);
  }

  public List<String> getPubmedIDs()
  {
    return Collections.unmodifiableList(pubmedIDs);
  }

  public Map<String, Map<String, String>> getVariables()
  {
    return Collections.unmodifiableMap(variables);
  }

  public Map<String, List<String>> getRepeat()
  {
    return Collections.unmodifiableMap(repeat);
  }

  @Override public String toString()
  {
    return "Series{" +
      "\n title='" + title + '\'' +
      "\n summary='" + summary + '\'' +
      "\n overallDesign='" + overallDesign + '\'' +
      "\n contributors=" + contributors +
      "\n pubmedIDs=" + pubmedIDs +
      "\n variables=" + variables +
      "\n repeat=" + repeat +
      '}';
  }
}
