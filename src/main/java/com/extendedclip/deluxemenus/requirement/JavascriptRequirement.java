package com.extendedclip.deluxemenus.requirement;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.menu.MenuHolder;
import com.extendedclip.deluxemenus.utils.DebugLevel;
import java.util.logging.Level;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

public class JavascriptRequirement extends Requirement {

  private final ScriptEngineFactory factory = new NashornScriptEngineFactory();
  private final ServicesManager manager = Bukkit.getServer().getServicesManager();
  private static ScriptEngineManager engine;
  private final String expression;

  public JavascriptRequirement(String expression) {
    this.expression = expression;
    if (engine == null) {
      if (manager.isProvidedFor(ScriptEngineManager.class)) {
        final RegisteredServiceProvider provider = manager.getRegistration(ScriptEngineManager.class);
        engine = (ScriptEngineManager) provider.getProvider();
      } else {
        engine = new ScriptEngineManager();
        manager.register(ScriptEngineManager.class, engine, DeluxeMenus.getInstance(), ServicePriority.Highest);
      }
      engine.registerEngineName("JavaScript", factory);
      engine.put("BukkitServer", Bukkit.getServer());
    }
  }

  @Override
  public boolean evaluate(MenuHolder holder) {

    String exp = holder.setPlaceholders(expression);
    try {

      engine.put("BukkitPlayer", holder.getViewer());
      Object result = engine.getEngineByName("JavaScript").eval(exp);

      if (!(result instanceof Boolean)) {
        DeluxeMenus.debug(
            DebugLevel.HIGHEST,
            Level.WARNING,
            "Requirement javascript <" + this.expression + "> is invalid and does not return a boolean!"
        );
        return false;
      }

      return (boolean) result;

    } catch (final ScriptException | NullPointerException exception) {
      DeluxeMenus.debug(
          DebugLevel.HIGHEST,
          Level.WARNING,
          "Error in requirement javascript syntax - " + this.expression
      );

      DeluxeMenus.printStacktrace(
          "Error in requirement javascript syntax - " + this.expression,
          exception
      );
      return false;
    }
  }

}
