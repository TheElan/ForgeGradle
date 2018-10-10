package net.minecraftforge.gradle.userdev;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.logging.Logger;

import net.minecraftforge.gradle.common.util.BaseRepo;
import net.minecraftforge.gradle.common.util.MinecraftRepo;
import net.minecraftforge.gradle.mcp.MCPRepo;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class UserDevPlugin implements Plugin<Project> {
    private static String MINECRAFT = "minecraft";

    @Override
    public void apply(@Nonnull Project project) {
        @SuppressWarnings("unused")
        final Logger logger = project.getLogger();
        final UserDevExtension extension = project.getExtensions().create("minecraft", UserDevExtension.class, project);
        if (project.getPluginManager().findPlugin("java") == null) {
            project.getPluginManager().apply("java");
        }
        //final File natives_folder = project.file("build/natives/");
        /* TODO: Make compile/jar tasks reobf?
        final JavaPluginConvention javaConv = (JavaPluginConvention)project.getConvention().getPlugins().get("java");

        Jar jarConfig = (Jar)project.getTasks().getByName("jar");
        JavaCompile javaCompile = (JavaCompile)project.getTasks().getByName("compileJava");
        */


        Configuration minecraft = project.getConfigurations().maybeCreate(MINECRAFT);
        Configuration compile = project.getConfigurations().maybeCreate("compile");
        compile.extendsFrom(minecraft);

        project.afterEvaluate(p -> {
            MinecraftUserRepo mcrepo = null;

            //TODO: UserDevRepo deobf = new UserDevRepo(project);

            DependencySet deps = minecraft.getDependencies();
            for (Dependency dep : deps.stream().collect(Collectors.toList())) {
                if (!(dep instanceof ExternalModuleDependency))
                    throw new IllegalArgumentException("minecraft dependency must be a maven dependency.");
                if (mcrepo != null)
                    throw new IllegalArgumentException("Only allows one minecraft dependancy.");
                deps.remove(dep);

                mcrepo = new MinecraftUserRepo(p, dep.getGroup(), dep.getName(), dep.getVersion(), extension.getAccessTransformers(), extension.getMappings());
                String newDep = mcrepo.getDependencyString();
                p.getLogger().lifecycle("New Dep: " + newDep);
                ExternalModuleDependency ext = (ExternalModuleDependency)p.getDependencies().create(newDep);
                {
                    ext.setChanging(true); //TODO: Remove when not in dev
                    minecraft.resolutionStrategy(strat -> {
                        strat.cacheChangingModulesFor(0, TimeUnit.MINUTES);
                    });
                }
                minecraft.getDependencies().add(ext);
            }


            // We have to add these AFTER our repo so that we get called first, this is annoying...
            new BaseRepo.Builder()
                .add(mcrepo)
                .add(MCPRepo.create(project))
                .add(MinecraftRepo.create(project)) //Provides vanilla extra/slim/data jars. These don't care about OBF names.
                .attach(project);
            project.getRepositories().maven(e -> {
                e.setUrl("http://files.minecraftforge.net/maven/");
            });
            project.getRepositories().maven(e -> {
                e.setUrl("https://libraries.minecraft.net/");
                e.metadataSources(src -> src.artifact());
            });
            project.getRepositories().mavenCentral(); //Needed for MCP Deps
        });
    }

}