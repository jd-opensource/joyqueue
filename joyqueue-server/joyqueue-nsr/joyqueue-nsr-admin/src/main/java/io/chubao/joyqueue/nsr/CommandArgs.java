package io.chubao.joyqueue.nsr;

import com.beust.jcommander.Parameter;

public class CommandArgs {
    @Parameter(names = {"-h", "--help"}, description = "Help message", help = true)
    public boolean help;
    @Parameter(names = { "--host" }, description = "Naming address", required = false)
    public String host="http://localhost:50091";
}
