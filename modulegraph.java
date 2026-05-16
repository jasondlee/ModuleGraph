///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.aesh:aesh:3.7
//DEPS com.steeplesoft:modulegraph:0.2.1


import org.aesh.AeshRuntimeRunner;
import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Argument;
import com.steeplesoft.modulegraph.ModuleGraph;

@CommandDefinition(name = "modulegraph",
        description = "modulegraph made with jbang",
        version = "0.2.1",
        generateHelp = true)
public class modulegraph implements Command<CommandInvocation> {

    @Argument(description = "The JBoss Modules directory to process")
    private String moduleDir;

    public static void main(String... args) {
        AeshRuntimeRunner.builder()
                .command(modulegraph.class)
                .args(args)
                .execute();
    }

    @Override
    public CommandResult execute(CommandInvocation invocation) {
        try {
            new ModuleGraph().run(moduleDir);
            return CommandResult.SUCCESS;
        } catch (Exception e) {
            invocation.println("Error processing module directory: " + e.getMessage());
            return CommandResult.FAILURE;
        }
    }
}
