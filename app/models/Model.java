package models;

import play.Play;
import uk.co.panaxiom.playjongo.PlayJongo;

/**
 * Created by Win7 on 23-4-2016.
 */
public class Model {

    public static PlayJongo jongo = Play.application().injector().instanceOf(PlayJongo.class);

}
