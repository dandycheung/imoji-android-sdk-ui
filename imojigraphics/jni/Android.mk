LOCAL_PATH := $(call my-dir)/../../../

include $(CLEAR_VARS)

LOCAL_CFLAGS += -DNDEBUG -std=gnu99 -ffast-math -ftree-vectorize \
				-iquote $(LOCAL_PATH)/NanoVG \
				-iquote $(LOCAL_PATH)/Clipper
LOCAL_CPP_FEATURES += exceptions
LOCAL_LDLIBS := -llog -ljnigraphics -lGLESv2 -lEGL

LOCAL_MODULE    := imojigraphics
LOCAL_SRC_FILES := \
	NanoVG/nanovg.c \
	Clipper/clipper.cpp \
	ImojiGraphics/IGCanvas.c \
	ImojiGraphics/IGClipper.cpp \
	ImojiGraphics/IGContext.c \
	ImojiGraphics/IGImage.c \
	ImojiGraphics/IGPath.cpp \
	ImojiGraphics/IGPaths.cpp \
	ImojiGraphics/IGPoint.c \
	ImojiGraphics/IGTrace.cpp \
	ImojiGraphics/IGBorder.c \
	ImojiGraphics/IGShadow.c \
	ImojiGraphics/IGEditor.c \
	ImojiGraphics/IGWebP.cpp \
	ImojiGraphics/Vector.c

include $(BUILD_SHARED_LIBRARY)
