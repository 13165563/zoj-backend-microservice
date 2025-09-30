<template>
  <div class="item">
    <h3>
      <slot name="heading" />
    </h3>
    <slot />
  </div>
</template>

<style scoped>
.item {
  padding: 20px;
  border: none;
  background-color: #ffffff;
  color: #000000;
}

.item h3 {
  margin-top: 0;
  color: #000000;
}

.item a {
  color: #000000;
  text-decoration: none;
}

.item a:hover {
  background-color: rgba(0, 0, 0, 0.1);
}
</style>
