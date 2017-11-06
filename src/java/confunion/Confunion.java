package confunion;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Façade to access Confunion functions through Java.
 */
@SuppressWarnings("unused")
public class Confunion {
  private final IFn addPropertiesFn;
  private final IFn loadConfigurationFn;
  private final IFn composeConfiguration;

  public Confunion() {
    IFn require = Clojure.var("clojure.core", "require");

    require.invoke(Clojure.read("confunion.core"));
    loadConfigurationFn = Clojure.var("confunion.core", "load-configuration");
    composeConfiguration = Clojure.var("confunion.core", "compose-configuration");

    require.invoke(Clojure.read("confunion.properties"));
    addPropertiesFn = Clojure.var("confunion.properties", "add-properties");
  }

  /**
   * Builds a configuration map by merging the contents of the base EDN file content with the first existing override
   * one.
   *
   * @param schemaPath Path of the EDN file that describes the structure of the final configuration.
   * @param basePaths Ordered paths list of the possible base EDN configurations files (NB: at least one must exists).
   * @param overridePaths Ordered paths list of possible overrides EDN file, that will be merged with the base one.
   * @return Validated configuration (obtained by merging the base with the override one) or an exception
   */
  public Map loadConfiguration(String schemaPath, List<String> basePaths, List<String> overridePaths) {
    return (Map) loadConfigurationFn.invoke(schemaPath, basePaths, overridePaths);
  }

  /**
   * Populate {@link Properties} using a configuration map.
   * <p>
   * If a property specified in the configuration already exists in the input {@link Properties}, it'll be overwritten.
   * </p>
   *
   * @param p {@link java.util.Properties} object to populate
   * @param m Configuration map that will be "serialized" in the input {@link Properties} object. Keywords and values
   *          will be serialized in plain strings.
   */
  public void addProperties(Properties p, Map m) {
    addPropertiesFn.invoke(p, m);
  }

  /**
   * Builds a configuration map by merging the contents of the base EDN file content with the first existing override
   * one.
   *
   * @param schemaPath Ordered list of paths to look for the base schema EDN file.
   * @param additionalSchemaPath Ordered list of paths look for the (optional) additional schema EDN file to merge with
   *          the base one.
   * @param basePaths Ordered paths list of the possible base EDN configurations files (NB: at least one must exists).
   * @param overridePaths Ordered paths list of possible overrides EDN file, that will be merged with the base one.
   * @return Validated configuration (obtained by merging the base with the override one) or an exception
   */
  public Map composeConfiguration(List<String> schemaPath, List<String> additionalSchemaPath, List<String> basePaths,
      List<String> overridePaths) {
    return (Map) composeConfiguration.invoke(schemaPath, additionalSchemaPath, basePaths, overridePaths);
  }
}
