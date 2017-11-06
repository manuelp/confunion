package confunion;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import java.util.Map;

/**
 * API a più basso livello di {@link confunion.Confunion}
 */
@SuppressWarnings("unused")
public class ConfunionEngine {

  private final IFn applyFn;
  private final IFn confunionFn;
  private final IFn verifyConfFn;
  private final IFn verifySchemaFn;
  private final IFn loadEdnFn;

  public ConfunionEngine() {
    IFn require = Clojure.var("clojure.core", "require");

    applyFn = Clojure.var("clojure.core", "apply");

    require.invoke(Clojure.read("confunion.edn"));
    loadEdnFn = Clojure.var("confunion.edn", "load-edn");

    require.invoke(Clojure.read("confunion.core"));
    confunionFn = Clojure.var("confunion.core", "confunion");

    require.invoke(Clojure.read("confunion.schema"));
    verifySchemaFn = Clojure.var("confunion.schema", "verify-schema");
    verifyConfFn = Clojure.var("confunion.schema", "verify-conf");
  }

  /**
   * Builds a configuration map by merging multiple EDN map files in the given paths, loaded from first to last.
   *
   * @param paths Paths of the files that contain EDN configuration maps.
   * @return Merged configuration map
   */
  public Map confunion(String... paths) {
    return (Map) applyFn.invoke(confunionFn, paths);
  }

  /**
   * Read an EDN file.
   *
   * @param path Path of the EDN file
   * @return Object that represents the top-level data structure defined in the input EDN file (list, map, set, etc)
   */
  public Object loadEdn(String path) {
    return loadEdnFn.invoke(path);
  }

  /**
   * Validate a schema for well-formedness.
   *
   * @param schema Configuration specification
   * @return The input schema if well-formed according to the specification, otherwise throws an exception with a
   *         detailed error message explaining the validation errors.
   */
  public Map validateSchema(Map schema) {
    return (Map) verifySchemaFn.invoke(schema);
  }

  /**
   * Validate a configuration against a schema.
   *
   * @param configuration Configuration map
   * @param schema Schema (configuration specification)
   * @return The input configuration if well-formed according the given schema, otherwise throwns an exception with a
   *         detailed error message explaining the validation errors.
   */
  public Map validateConfiguration(Map configuration, Map schema) {
    return (Map) verifyConfFn.invoke(configuration, schema);
  }
}
