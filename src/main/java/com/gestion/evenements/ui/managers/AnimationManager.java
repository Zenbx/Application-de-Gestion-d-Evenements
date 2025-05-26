package com.gestion.evenements.ui.managers;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

/**
 * Gestionnaire centralisé pour toutes les animations de l'interface
 * Version robuste avec gestion d'erreurs
 */
public class AnimationManager {
    
    private Timeline currentAnimation;
    
    public void fadeIn(Node node, Duration duration) {
        if (node == null) return;
        
        try {
            node.setOpacity(0);
            
            FadeTransition fade = new FadeTransition(duration, node);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        } catch (Exception e) {
            System.err.println("Erreur animation fadeIn: " + e.getMessage());
            // Fallback: afficher directement sans animation
            node.setOpacity(1);
        }
    }
    
    public void fadeOut(Node node, Duration duration, Runnable onFinished) {
        if (node == null) return;
        
        try {
            FadeTransition fade = new FadeTransition(duration, node);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> {
                if (onFinished != null) {
                    try {
                        onFinished.run();
                    } catch (Exception ex) {
                        System.err.println("Erreur callback fadeOut: " + ex.getMessage());
                    }
                }
            });
            fade.play();
        } catch (Exception e) {
            System.err.println("Erreur animation fadeOut: " + e.getMessage());
            // Exécuter le callback quand même
            if (onFinished != null) {
                onFinished.run();
            }
        }
    }
    
    public void slideInFromRight(Node node, Duration duration) {
        if (node == null) return;
        
        try {
            node.setTranslateX(300);
            node.setOpacity(0);
            
            Timeline slide = new Timeline(
                new KeyFrame(duration,
                    new KeyValue(node.translateXProperty(), 0),
                    new KeyValue(node.opacityProperty(), 1)
                )
            );
            slide.play();
        } catch (Exception e) {
            System.err.println("Erreur animation slideInFromRight: " + e.getMessage());
            // Fallback: afficher directement
            node.setTranslateX(0);
            node.setOpacity(1);
        }
    }
    
    public void slideInFromLeft(Node node, Duration duration) {
        if (node == null) return;
        
        try {
            node.setTranslateX(-300);
            node.setOpacity(0);
            
            Timeline slide = new Timeline(
                new KeyFrame(duration,
                    new KeyValue(node.translateXProperty(), 0),
                    new KeyValue(node.opacityProperty(), 1)
                )
            );
            slide.play();
        } catch (Exception e) {
            System.err.println("Erreur animation slideInFromLeft: " + e.getMessage());
            node.setTranslateX(0);
            node.setOpacity(1);
        }
    }
    
    public void scaleIn(Node node, Duration duration) {
        if (node == null) return;
        
        try {
            node.setScaleX(0.8);
            node.setScaleY(0.8);
            node.setOpacity(0);
            
            Timeline scale = new Timeline(
                new KeyFrame(duration,
                    new KeyValue(node.scaleXProperty(), 1),
                    new KeyValue(node.scaleYProperty(), 1),
                    new KeyValue(node.opacityProperty(), 1)
                )
            );
            scale.play();
        } catch (Exception e) {
            System.err.println("Erreur animation scaleIn: " + e.getMessage());
            node.setScaleX(1);
            node.setScaleY(1);
            node.setOpacity(1);
        }
    }
    
    public void scrollToTop(ScrollPane scrollPane) {
        if (scrollPane == null) return;
        
        try {
            Timeline scroll = new Timeline(
                new KeyFrame(Duration.millis(300),
                    new KeyValue(scrollPane.vvalueProperty(), 0),
                    new KeyValue(scrollPane.hvalueProperty(), 0)
                )
            );
            scroll.play();
        } catch (Exception e) {
            System.err.println("Erreur animation scrollToTop: " + e.getMessage());
            // Fallback: scroll direct sans animation
            scrollPane.setVvalue(0);
            scrollPane.setHvalue(0);
        }
    }
    
    public void highlightNode(Node node) {
        if (node == null) return;
        
        try {
            ScaleTransition pulse = new ScaleTransition(Duration.millis(200), node);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.05);
            pulse.setToY(1.05);
            pulse.setCycleCount(2);
            pulse.setAutoReverse(true);
            pulse.play();
        } catch (Exception e) {
            System.err.println("Erreur animation highlightNode: " + e.getMessage());
        }
    }
    
    public void shakeNode(Node node) {
        if (node == null) return;
        
        try {
            Timeline shake = new Timeline(
                new KeyFrame(Duration.millis(0), new KeyValue(node.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(50), new KeyValue(node.translateXProperty(), -5)),
                new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), 5)),
                new KeyFrame(Duration.millis(150), new KeyValue(node.translateXProperty(), -5)),
                new KeyFrame(Duration.millis(200), new KeyValue(node.translateXProperty(), 0))
            );
            shake.play();
        } catch (Exception e) {
            System.err.println("Erreur animation shakeNode: " + e.getMessage());
        }
    }
    
    public void progressiveLoad(Node... nodes) {
        if (nodes == null || nodes.length == 0) return;
        
        try {
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (node != null) {
                    Timeline delay = new Timeline(
                        new KeyFrame(Duration.millis(i * 100), e -> {
                            fadeIn(node, Duration.millis(300));
                            slideInFromRight(node, Duration.millis(300));
                        })
                    );
                    delay.play();
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur animation progressiveLoad: " + e.getMessage());
            // Fallback: afficher tous les nodes directement
            for (Node node : nodes) {
                if (node != null) {
                    node.setOpacity(1);
                    node.setTranslateX(0);
                }
            }
        }
    }
    
    public void cleanup() {
        try {
            if (currentAnimation != null) {
                currentAnimation.stop();
                currentAnimation = null;
            }
        } catch (Exception e) {
            System.err.println("Erreur nettoyage animations: " + e.getMessage());
        }
    }
}