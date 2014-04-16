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
   * Costruisce una mappa di configurazione facendo il merge del contenuto delle
   * mappe EDN contenute nei file ai percorsi indicati, dal primo (base) all'ultimo).
   *
   * @param paths Numero variabile di path dei file EDN da cui leggere le mappe di configurazione
   * @return Mappa unione delle strutture dati contenute ai file indicati
   */
  public Map confunion(String... paths) {
    return (Map) applyFn.invoke(confunionFn, paths);
  }

  /**
   * Legge un file in formato EDN.
   *
   * @param path Path del file EDN da leggere
   * @return Oggetto che rappresenta la struttura dati top-level definita
   * nel file EDN (lista, mappa, set, ecc)
   */
  public Object loadEdn(String path) {
    return loadEdnFn.invoke(path);
  }

  /**
   * Valida uno schema per controllare se è ben formato.
   *
   * @param schema Mappa contenente la definizione dei parametri di configurazione
   * @return Schema se ben formato, altrimenti genera un'eccezione con un messaggio
   * di errore dettagliato con tutti gli errori di validazione riscontrati.
   */
  public Map validateSchema(Map schema) {
    return (Map) verifySchemaFn.invoke(schema);
  }

  /**
   * Valida una configurazione usando uno schema.
   *
   * @param configuration Mappa contenente la configurazione
   * @param schema        Mappa contenente la definizione dei parametri di configurazione
   * @return La configurazione stessa se corretta, altrimenti viene lanciata un'eccezione con
   * un messaggio che dettagli quali sono gli errori di validazione riscontrati
   */
  public Map validateConfiguration(Map configuration, Map schema) {
    return (Map) verifyConfFn.invoke(configuration, schema);
  }
}
