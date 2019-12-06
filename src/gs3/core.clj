(ns gs3.core
  (:import [org.lwjgl.glfw GLFW GLFWKeyCallback GLFWErrorCallback]
           [org.lwjgl.opengl GL]
           [org.lwjgl.opengl GL33]))

(defn handle-key
  [window k scancode action mods]
  (when (and (= k GLFW/GLFW_KEY_ESCAPE)
             (= action GLFW/GLFW_RELEASE))
    (GLFW/glfwSetWindowShouldClose window true)))

(defn init-window
  [width height title]
  (.set (GLFWErrorCallback/createPrint System/err))
  (when-not (GLFW/glfwInit)
    (throw (ex-info "Error initializing GLFW")))
  (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE GLFW/GLFW_FALSE)
  (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_TRUE)
  (let [window       (GLFW/glfwCreateWindow width height title 0 0)
        key-callback (proxy [GLFWKeyCallback] []
                       (invoke [win k scancode action mods]
                         (#'handle-key win k scancode action mods)))]
    (GLFW/glfwSetKeyCallback window key-callback)
    (GLFW/glfwMakeContextCurrent window)
    (GLFW/glfwSwapInterval 1)
    (GLFW/glfwShowWindow window)
    window))

(defn draw
  []
  (GL33/glClearColor 0.0 0.0 0.0 0.0)
  (GL33/glClear (bit-or GL33/GL_COLOR_BUFFER_BIT GL33/GL_DEPTH_BUFFER_BIT)))

(defn render-loop
  [window]
  (GL/createCapabilities)
  (while (not (GLFW/glfwWindowShouldClose window))
    (#'draw)
    (GLFW/glfwSwapBuffers window)
    (GLFW/glfwPollEvents)))

(defn run
  []
  (let [window (init-window 640 480 "GS3")]
    (render-loop window)
    (GLFW/glfwDestroyWindow window)
    (GLFW/glfwTerminate)
    (.free (GLFW/glfwSetErrorCallback nil))))
