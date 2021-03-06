import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.H2Platform;
import com.avaje.ebean.config.dbplatform.PostgresPlatform;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import controllers.ApplicationController;
import models.User;
import play.*;
import play.libs.F;
import play.libs.Yaml;
import play.mvc.*;
import play.mvc.Http.*;

import java.util.*;

import static play.mvc.Results.internalServerError;

public class Global extends GlobalSettings {
    private static final boolean LOAD_TEST_DATA = true;
    @Override
    public void onStart(Application app) {
        if (LOAD_TEST_DATA && !isJUnitTest()) {
            cleanDatabase();
            fillDatabase("testFiles/placeholder-data.yml");
            Logger.info("test data loaded");
        }
    }
    @Override
    public F.Promise<Result> onBadRequest(RequestHeader request, String error) {
        return F.Promise.<Result>pure(ApplicationController.errorBadRequest(error));
    }
    @Override
    public F.Promise<Result> onHandlerNotFound(RequestHeader request) {
        return F.Promise.<Result>pure(ApplicationController.errorNotFound(
            "Die Seite '" + request.path() + "' wurde nicht gefunden."
        ));
    }
    @Override
    public F.Promise<Result> onError(RequestHeader request, Throwable t) {
        Logger.error(t.toString());
        return F.Promise.<Result>pure(internalServerError(
            views.html.error.render(500, t.toString())
        ));
    }


    private  void cleanDatabase(){
        EbeanServer server = Ebean.getServer("default");
        ServerConfig config = new ServerConfig();
        DdlGenerator ddl = new DdlGenerator();
        String databaseType = Play.application().configuration().getString("databaseType");
        if (databaseType.equals("postgres")) {
            ddl.setup((SpiEbeanServer) server, new PostgresPlatform(), config);
        } else {
            ddl.setup((SpiEbeanServer) server, new H2Platform(), config);
        }
        ddl.runScript(false, ddl.generateDropDdl());
        ddl.runScript(false, ddl.generateCreateDdl());
        assert User.find.all().size() == 0;
    }

    private  void fillDatabase(String yamlFile) {
        Object yam = Yaml.load(yamlFile);
        if (yam instanceof ArrayList) {
            Ebean.save((List) yam);
        } else {
            Map<String, List<Object>> yamMap = (Map) yam;
            for (String s : yamMap.keySet()) {
                Ebean.save(yamMap.get(s));
            }
        }
    }
    private  boolean isJUnitTest() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> list = Arrays.asList(stackTrace);
        for (StackTraceElement element : list) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }
}
