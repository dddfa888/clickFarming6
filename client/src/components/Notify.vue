<template>
  <div
    v-if="visible"
    :class="['notify', type]"
    @mouseenter="pauseTimer"
    @mouseleave="resumeTimer"
  >
    <div class="notify-content">
      <!-- 左侧图标 -->
      <div class="text-content">
        <strong class="title">{{ title }}</strong>
        <p class="message">{{ message }}</p>
      </div>
    </div>

    <span class="close-btn" @click="close">×</span>
    <div class="progress-bar" :style="progressBarStyle"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";

const props = defineProps({
  title: { type: String, default: "" },
  message: { type: String, default: "" },
  duration: { type: Number, default: 3000 },
  type: {
    type: String,
    default: "info", // success, warning, error, info
    validator: (val) => ["success", "warning", "error", "info"].includes(val),
  },
});

const emit = defineEmits(["close"]);

const visible = ref(true);
let timer = null;

// 图标映射
// const iconMap = {
//   success: "✔️",
//   warning: "⚠️",
//   error: "❌",
//   info: "ℹ️",
// };

// const icon = computed(() => iconMap[props.type]);

// 进度控制
const progress = ref(100);
let animationFrameId = null;
let startTime = 0;
let remainingTime = props.duration;

const updateProgress = (timestamp) => {
  if (!startTime) startTime = timestamp;
  const elapsed = timestamp - startTime;
  const percent = Math.max(100 - (elapsed / props.duration) * 100, 0);
  progress.value = percent;

  if (percent > 0) {
    animationFrameId = requestAnimationFrame(updateProgress);
  }
};

const startTimer = () => {
  if (props.duration > 0) {
    startTime = performance.now();
    remainingTime = props.duration;
    timer = setTimeout(close, props.duration);
    animationFrameId = requestAnimationFrame(updateProgress);
  }
};

const pauseTimer = () => {
  if (timer) clearTimeout(timer);
  if (animationFrameId) cancelAnimationFrame(animationFrameId);
  remainingTime = (progress.value / 100) * props.duration;
};

const resumeTimer = () => {
  if (remainingTime > 0) {
    startTime = performance.now();
    timer = setTimeout(close, remainingTime);
    animationFrameId = requestAnimationFrame(updateProgress);
  }
};

const close = () => {
  visible.value = false;
  emit("close");
};

const progressBarStyle = computed(() => ({
  width: `${progress.value}%`,
  transition: "width 0.1s linear",
}));

onMounted(startTimer);
onUnmounted(() => {
  if (timer) clearTimeout(timer);
  if (animationFrameId) cancelAnimationFrame(animationFrameId);
});

</script>
<style scoped>
.notify {
  position: fixed;
  right: 20px;
  top: 20px;
  min-width: 250px;
  padding: 12px 16px;
  border-radius: 4px;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: space-between;
  animation: fadeIn 0.3s ease;
  z-index: 999;
}
.notify .title {
  font-weight: bold;
}
.notify .message {
  margin: 4px 0 0;
}
.notify.success {
  background-color: #67c23a;
}
.notify.warning {
  background-color: #e6a23c;
}
.notify.error {
  background-color: #f56c6c;
}
.notify.info {
  background-color: #409eff;
}
.close-btn {
  cursor: pointer;
  font-size: 18px;
  margin-left: 12px;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

 .progress-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  background-color: #fff; /* 可替换为不同类型颜色 */
  border-radius: 0 0 4px 4px;
}

@media (min-width: 768px) {
  .notify {
    position: fixed;
    right: 20px;
    top: 20px;
    min-width: 250px;
    padding: 12px 16px;
    border-radius: 4px;
    color: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    display: flex;
    align-items: center;
    justify-content: space-between;
    animation: fadeIn 0.3s ease;
  }
  .notify .title {
    font-weight: bold;
  }
  .notify .message {
    margin: 4px 0 0;
  }
  .notify.success {
    background-color: #67c23a;
  }
  .notify.warning {
    background-color: #e6a23c;
  }
  .notify.error {
    background-color: #f56c6c;
  }
  .notify.info {
    background-color: #409eff;
  }
  .close-btn {
    cursor: pointer;
    font-size: 18px;
    margin-left: 12px;
  }
  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(-10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  .progress-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  background-color: #fff; /* 可替换为不同类型颜色 */
  border-radius: 0 0 4px 4px;
}
}
</style>
