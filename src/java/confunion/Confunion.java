package confunion;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Facade per accedere alle funzioni offerte dalla libreria tramite Java.
 */
@SuppressWarnings("unused")
public class Confunion {

  private final IFn applyFn;
  private final IFn confunionFn;
  private final IFn addPropertiesFn;
  private final IFn loadConfigurationFn;

  public Confunion() {
    IFn require = Clojure.var("clojure.core", "require");

    applyFn = Clojure.var("clojure.core", "apply");

    require.invoke(Clojure.read("confunion.core"));
    confunionFn = Clojure.var("confunion.core", "confunion");
    loadConfigurationFn = Clojure.var("confunion.core", "load-configuration");

    require.invoke(Clojure.read("confunion.properties"));
    addPropertiesFn = Clojure.var("confunion.properties", "add-properties");
  }

  /**
   * Costruisce una mappa di configurazione ottenuta effettuando il merge del file EDN di base
   * con il contenuto del primo esistente nella lista di overrides.
   *
   * @param schemaPath    Path del file EDN contenente lo schema che descrive la struttura che la configurazione
   *                      finale dovrà rispettare
   * @param basePaths     Lista *ordinata* di path dei possibili file EDN con la configurazione di base
   *                      (NB: almeno uno deve esistere)
   * @param overridePaths Lista *ordinata* di path dei possibili file EDN con la seconda mappa da unire alla prima
   * @return Unione della mappa di configurazione di base con eventualmente un'altra con gli
   * <em>overrides</em>
   */
  public Map loadConfiguration(String schemaPath, List<String> basePaths, List<String> overridePaths) {
    return (Map) loadConfigurationFn.invoke(schemaPath, basePaths, overridePaths);
  }

  /**
   * Aggiunge proprietà all'oggetto {@link java.util.Properties} specificato a partire
   * dalla mappa di configurazione passata (eventualmente sovrascrivendo quelle preesistenti).
   *
   * @param p Oggetto {@link java.util.Properties} nel quale inserire le proprietà
   * @param m Mappa con le proprietà da inserire, può essere una mappa in formato EDN e
   *          sia le chiavi che i valori verranno serializzati in stringhe.
   */
  public void addProperties(Properties p, Map m) {
    addPropertiesFn.invoke(p, m);
  }
}
