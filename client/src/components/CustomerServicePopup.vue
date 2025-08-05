<template>
  <div v-if="visible" class="popup-mask" @click.self="close">
    <div class="popup-container">
      <!-- Tawk.to 聊天窗口会自动显示在这里（右下角） -->
      <button class="close-btn" @click="close">×</button>
    </div>
  </div>
</template>

<script setup>
import { watch, onMounted, onBeforeUnmount } from "vue";

const props = defineProps({
  visible: Boolean
});
const emit = defineEmits(["close"]);

const close = () => {
  emit("close");
};

function showTawk() {
  if (window.Tawk_API) {
    window.Tawk_API.show(); // 显示聊天按钮（如果之前hide了）
    window.Tawk_API.maximize(); // 打开聊天窗口
  }
}

function hideTawk() {
  if (window.Tawk_API) {
    window.Tawk_API.minimize(); // 最小化聊天窗口
    window.Tawk_API.hide(); // 隐藏聊天按钮
  }
}

watch(
  () => props.visible,
  visible => {
    if (visible) {
      showTawk();
    } else {
      hideTawk();
    }
  }
);

onMounted(() => {
  if (props.visible) showTawk();
});

onBeforeUnmount(() => {
  hideTawk();
});
</script>

<style scoped>
.popup-mask {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.popup-container {
  width: 320px;
  height: 480px;
  background: white;
  border-radius: 8px;
  position: relative;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.3);
}

/* 关闭按钮 */
.close-btn {
  position: absolute;
  top: 6px;
  right: 10px;
  font-size: 24px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: #333;
  font-weight: bold;
}
</style>
