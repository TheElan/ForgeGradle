package net.minecraftforge.gradle.forgedev.mcp.util;

import org.gradle.internal.impldep.com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RawMCPConfig {

    public String mcVersion;
    public JsonObject data;
    public final Pipeline pipeline = new Pipeline();
    public final Map<String, Function> functions = new HashMap<>();
    public final Libraries libraries = new Libraries();

    public void addFunction(String name, String version, String repo, String[] jvmArgs, String[] runArgs, String[] envVars) {
        functions.put(name, new Function(version, repo, jvmArgs, runArgs, envVars));
    }

    public class Pipeline {

        public final List<Step> sharedSteps = new LinkedList<>();
        public final List<Step> srcSteps = new LinkedList<>();

        public void addShared(String type, Map<String, String> arguments) {
            sharedSteps.add(new Step(type, arguments));
        }

        public void addSrc(String type, Map<String, String> arguments) {
            srcSteps.add(new Step(type, arguments));
        }

        public class Step {

            public final String type;
            public final Map<String, String> arguments;

            public Step(String type, Map<String, String> arguments) {
                this.type = type;
                this.arguments = arguments;
            }

        }

    }

    public class Function {

        public final String version;
        public final String repo;

        public final String[] jvmArgs;
        public final String[] runArgs;
        public final String[] envVars;

        public Function(String version, String repo, String[] jvmArgs, String[] runArgs, String[] envVars) {
            this.version = version;
            this.repo = repo;
            this.jvmArgs = jvmArgs;
            this.runArgs = runArgs;
            this.envVars = envVars;
        }

    }

    public class Libraries {

        public final Set<String> client = new HashSet<>();
        public final Set<String> server = new HashSet<>();
        public final Set<String> joined = new HashSet<>();

    }

}