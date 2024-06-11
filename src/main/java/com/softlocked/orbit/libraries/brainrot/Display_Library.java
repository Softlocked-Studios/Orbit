package com.softlocked.orbit.libraries.brainrot;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.memory.ILocalContext;
import com.softlocked.orbit.utils.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Display_Library implements OrbitJavaLibrary {
    List<JFrame> frames = new ArrayList<>();
    List<Pair<BufferedImage, Graphics2D>> buffers = new ArrayList<>();

    @Override
    public void load(GlobalContext context) {
        context.addFunction(new NativeFunction("createFrame", 0, Variable.Type.ANY) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(800, 600);
                frames.add(frame);
                BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
                buffers.add(new Pair<>(image, image.createGraphics()));
                return frame;
            }
        });

        context.addFunction(new NativeFunction("createFrame", List.of(Variable.Type.INT, Variable.Type.INT), Variable.Type.ANY) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize((int) args.get(0), (int) args.get(1));
                frames.add(frame);
                BufferedImage image = new BufferedImage((int) args.get(0), (int) args.get(1), BufferedImage.TYPE_INT_RGB);
                buffers.add(new Pair<>(image, image.createGraphics()));
                return frame;
            }
        });

        context.addFunction(new NativeFunction("var.setVisible", List.of(Variable.Type.ANY, Variable.Type.BOOL), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                JFrame frame = (JFrame) args.get(0);
                frame.setVisible((boolean) args.get(1));
                return null;
            }
        });

        context.addFunction(new NativeFunction("var.setTitle", List.of(Variable.Type.ANY, Variable.Type.STRING), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                JFrame frame = (JFrame) args.get(0);
                frame.setTitle((String) args.get(1));
                return null;
            }
        });

        context.addFunction(new NativeFunction("var.closeFrame", List.of(Variable.Type.ANY), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                JFrame frame = (JFrame) args.get(0);
                frame.dispose();
                frames.remove(frame);
                return null;
            }
        });

        context.addFunction(new NativeFunction("var.setResizable", List.of(Variable.Type.ANY, Variable.Type.BOOL), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                JFrame frame = (JFrame) args.get(0);
                frame.setResizable((boolean) args.get(1));
                return null;
            }
        });

        context.addFunction(new NativeFunction("var.setUndecorated", List.of(Variable.Type.ANY, Variable.Type.BOOL), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                JFrame frame = (JFrame) args.get(0);
                frame.setUndecorated((boolean) args.get(1));
                return null;
            }
        });

        // Moving to the center of the screen
        context.addFunction(new NativeFunction("var.centerFrame", List.of(Variable.Type.ANY), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                JFrame frame = (JFrame) args.get(0);
                frame.setLocationRelativeTo(null);
                return null;
            }
        });

        // Drawing (pixels)
        context.addFunction(new NativeFunction("var.draw", List.of(Variable.Type.ANY, Variable.Type.INT, Variable.Type.INT, Variable.Type.INT), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                JFrame frame = (JFrame) args.get(0);
                Graphics g = buffers.get(frames.indexOf(frame)).second;
                int color = (int) args.get(3);
                g.setColor(new Color(color));
                g.drawRect((int) args.get(1), (int) args.get(2), 1, 1);
                return null;
            }
        });

        // Drawing (lines)
        context.addFunction(new NativeFunction("var.drawLine", List.of(Variable.Type.ANY, Variable.Type.INT, Variable.Type.INT, Variable.Type.INT, Variable.Type.INT, Variable.Type.INT), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                JFrame frame = (JFrame) args.get(0);
                Graphics g = buffers.get(frames.indexOf(frame)).second;
                int color = (int) args.get(5);
                g.setColor(new Color(color));
                g.drawLine((int) args.get(1), (int) args.get(2), (int) args.get(3), (int) args.get(4));
                return null;
            }
        });

        // Drawing (rect)
        context.addFunction(new NativeFunction("var.drawRect", List.of(Variable.Type.ANY, Variable.Type.INT, Variable.Type.INT, Variable.Type.INT, Variable.Type.INT, Variable.Type.INT), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                JFrame frame = (JFrame) args.get(0);
                Graphics g = buffers.get(frames.indexOf(frame)).second;
                int color = (int) args.get(5);
                g.setColor(new Color(color));
                g.fillRect((int) args.get(1), (int) args.get(2), (int) args.get(3), (int) args.get(4));
                return null;
            }
        });

        // Update
        context.addFunction(new NativeFunction("var.update", List.of(Variable.Type.ANY), Variable.Type.VOID) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                JFrame frame = (JFrame) args.get(0);
                frame.getContentPane().getGraphics().drawImage(buffers.get(frames.indexOf(frame)).first, 0, 0, null);
                return null;
            }
        });
    }
}
