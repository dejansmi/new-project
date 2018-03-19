package com.dejans.newproject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.FileWriter;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;


public class NewProjectCommands {
    private String[] args;
    String baseDir;
    String baseDirProject;
    String nameProject;

    public NewProjectCommands(String[] args) {
        this.args = args;
        this.baseDir = System.getenv("JAVA_PROJECTS_BASE");
    }

    public void execute() {
        if (args.length <= 0) {
            System.out.println("Syntax is: new-project nameOfProjects");
            return;
        }
        nameProject = args[0];
        baseDirProject = baseDir + "/source/" + nameProject;
        boolean de = new File(baseDirProject).exists();
        if (de) {
            System.out.println("Project (directory or file) " + nameProject + " exists.");
            return;
        } else {
            boolean mkd = new File(baseDirProject).mkdir();
            if (!mkd) {
                //TODO: SR: napraviti gresku 
            }
            try {
                Zip zip = new Zip();
                zip.zipDir(baseDir + "/new-project-template", baseDirProject + "/temp.zip");
                zip.unzip(baseDirProject, "/temp.zip", baseDirProject);
                File zipDel = new File(baseDirProject + "/temp.zip");
                zipDel.delete();
                File subFile = new File(baseDirProject);
                /* Create and adjust the configuration singleton */
                Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
                //cfg.setDirectoryForTemplateLoading(new File("Files"));
                cfg.setDefaultEncoding("UTF-8");
                cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
                cfg.setLogTemplateExceptions(false);
                cfg.setWrapUncheckedExceptions(true);

                subdirecotories(subFile, cfg);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void subdirecotories(File fileCurrent, Configuration cfg) throws IOException {
        if (fileCurrent.isDirectory()) {
            File[] children = fileCurrent.listFiles();
            for (File childFile : children) {
                subdirecotories(childFile, cfg);
            }
            return;
        }
        // First change name of files (if there are have $name$)
        if (fileCurrent.getName()!= null && fileCurrent.getName().contains("$name")) {
            String newName = nameProject;
            while (newName.contains("-") || newName.contains(" ") || newName.contains("#")) {
                int ind = newName.indexOf("-");
                if (ind > -1) {
                    newName = newName.substring(0,1).toUpperCase() 
                            + (newName.substring(0,ind).length()>1 ?newName.substring(1,ind):"") 
                            + (newName.substring(ind+1).length()>0?newName.substring(ind+1,ind+2).toUpperCase()
                            + (newName.substring(ind+1).length()>1?newName.substring(ind+2):""):"");
                }
            }
            newName = fileCurrent.getName().replace("$name$", newName);
            File newFile = new File(fileCurrent.getParentFile(),newName);
            fileCurrent.renameTo(newFile);
            fileCurrent = new File(fileCurrent.getParentFile(), newName);
        }
        /* Get the template (uses cache internally) */
        cfg.setDirectoryForTemplateLoading(fileCurrent.getParentFile());
        Template temp = cfg.getTemplate(fileCurrent.getName());

        File dir = new File(fileCurrent.getPath()+"XXX");
        BufferedWriter out = new BufferedWriter(new FileWriter(dir, false));
        Map root = new HashMap();
        root.put ("basedir","${basedir}");
        try {
			temp.process(root, out);
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        String st1 = fileCurrent.getPath();
        String st2 = fileCurrent.getParent();
        System.out.println(st1);
        System.out.println(st2);
    }

}
