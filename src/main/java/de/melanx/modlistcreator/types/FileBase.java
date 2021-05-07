package de.melanx.modlistcreator.types;

import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.minecraft.modpack.CurseModpack;
import com.therandomlabs.curseapi.project.CurseMember;
import com.therandomlabs.curseapi.project.CurseProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public abstract class FileBase {
    protected final StringBuilder builder;
    protected final CurseModpack pack;
    protected final boolean detailed;
    protected final boolean headless;
    protected final Map<CurseProject, CurseFile> projects = new HashMap<>();

    protected FileBase(CurseModpack pack, boolean detailed, boolean headless) {
        this.builder = new StringBuilder();
        this.pack = pack;
        this.detailed = detailed;
        this.headless = headless;
        try {
            this.pack.files().forEach(file -> {
                try {
                    CurseProject project = file.project();
                    this.projects.put(project, file);
                    System.out.println("[\u001B[32m" + this.pack.name() + "\u001B[0m] \u001B[33m" + (this.detailed ? file.displayName() : project.name()) + "\u001B[0m found");
                } catch (CurseException e) {
                    e.printStackTrace();
                }
            });
            this.projects.entrySet().stream()
                    .sorted((o1, o2) -> o1.getKey().name().compareTo(o2.getKey().toString()));
        } catch (CurseException e) {
            e.printStackTrace();
        }
    }

    public abstract void generateFile(String name, File output);

    protected abstract String getFormattedProject(CurseProject project, CurseFile file);

    protected abstract String getFormattedAuthor(CurseMember member);

    public abstract String getExtension();

    protected void generateFinalFile(String name, File output) {
        if (this.builder.toString().isEmpty()) {
            throw new IllegalStateException("Nothing to write to the file!");
        }
        try {
            if (!output.exists()) {
                if (output.mkdirs()) {
                    System.out.println("Created output directory: " + output);
                }
            }
            File file = new File(Paths.get(output.toString()) + File.separator + name + "." + this.getExtension());
            FileWriter writer = new FileWriter(file);
            writer.write(this.getContent());
            writer.close();
            System.out.println("Successfully generated " + file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getContent() {
        return this.builder.toString();
    }

    protected String getHeader() {
        return String.format("%s - %s", this.pack.name(), this.pack.version());
    }
}
