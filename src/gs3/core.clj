(ns gs3.core
  (:import [org.lwjgl.glfw GLFW GLFWKeyCallback GLFWMouseButtonCallback GLFWErrorCallback Callbacks]
           [org.lwjgl.opengl GL]
           [org.lwjgl.opengl GL33]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sending commands (s-expressions) to main OpenGL thread

(def command-queue (atom []))

(defn flush-command-queue!
  []
  (doseq [[cmd cb] @command-queue]
    (try
      (cb (eval cmd))
      (catch Exception e
        (println e))))
  (reset! command-queue []))

(defn enqueue-command!
  [cmd cb]
  (swap! command-queue conj [cmd cb]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Shader compilation

(defn compile-shader
  [shader-type source-path]
  (let [source  (slurp source-path)
        gl-type (case shader-type
                  :vert GL33/GL_VERTEX_SHADER
                  :frag GL33/GL_FRAGMENT_SHADER)
        shader  (GL33/glCreateShader gl-type)
        _       (GL33/glShaderSource shader source)
        _       (GL33/glCompileShader shader)
        status  (GL33/glGetShaderi shader GL33/GL_COMPILE_STATUS)
        fail?   (zero? status)]
    (if-not fail?
      shader
      (let [info (GL33/glGetShaderInfoLog shader)]
        (println "SHADER ERROR: " (.trim info))
        nil))))

(defn link-program
  [& shaders]
  (let [program (GL33/glCreateProgram)]
    (doseq [shdr shaders]
      (GL33/glAttachShader program shdr))
    (GL33/glLinkProgram program)
    (let [status (GL33/glGetProgrami program GL33/GL_LINK_STATUS)
          fail?  (zero? status)]
      (doseq [shdr shaders]
        (GL33/glDeleteShader shdr))
      (if-not fail?
        program
        (let [info (GL33/glGetProgramInfoLog program)]
          (println "LINKER ERROR: " (.trim info))
          nil)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Input event handlers

(defn handle-mouse
  [window button action mods]
  (when (= action GLFW/GLFW_PRESS)
    #_(println "Mouse clicked!")))

(defn handle-key
  [window k scancode action mods]
  (when (and (= k GLFW/GLFW_KEY_ESCAPE)
             (= action GLFW/GLFW_RELEASE))
    (GLFW/glfwSetWindowShouldClose window true)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Render loop and drawing

(defn draw
  []
  (GL33/glClearColor 0.0 0.0 0.0 0.0)
  (GL33/glClear (bit-or GL33/GL_COLOR_BUFFER_BIT GL33/GL_DEPTH_BUFFER_BIT)))

(defn render-loop
  [window]
  (GL/createCapabilities)
  (while (not (GLFW/glfwWindowShouldClose window))
    (flush-command-queue!)
    (#'draw)
    (GLFW/glfwSwapBuffers window)
    (GLFW/glfwPollEvents)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Window/context creation

(defn init-window
  [width height title]
  (.set (GLFWErrorCallback/createPrint System/err))
  (when-not (GLFW/glfwInit)
    (throw (ex-info "Error initializing GLFW")))
  (GLFW/glfwWindowHint GLFW/GLFW_CONTEXT_VERSION_MAJOR 3)
  (GLFW/glfwWindowHint GLFW/GLFW_CONTEXT_VERSION_MINOR 2)
  (GLFW/glfwWindowHint GLFW/GLFW_OPENGL_PROFILE GLFW/GLFW_OPENGL_CORE_PROFILE)
  (GLFW/glfwWindowHint GLFW/GLFW_OPENGL_FORWARD_COMPAT GL33/GL_TRUE)
  (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE GLFW/GLFW_FALSE)
  (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_TRUE)
  (let [window         (GLFW/glfwCreateWindow width height title 0 0)
        mouse-callback (proxy [GLFWMouseButtonCallback] []
                         (invoke [win button action mods]
                           (#'handle-mouse win button action mods)))
        key-callback   (proxy [GLFWKeyCallback] []
                         (invoke [win k scancode action mods]
                           (#'handle-key win k scancode action mods)))]
    (GLFW/glfwSetMouseButtonCallback window mouse-callback)
    (GLFW/glfwSetKeyCallback window key-callback)
    (GLFW/glfwMakeContextCurrent window)
    (GLFW/glfwSwapInterval 1)
    (GLFW/glfwShowWindow window)
    window))

(defn run
  []
  (let [window (init-window 640 480 "GS3")]
    (render-loop window)
    (Callbacks/glfwFreeCallbacks window)
    (GLFW/glfwDestroyWindow window)
    (GLFW/glfwTerminate)))
