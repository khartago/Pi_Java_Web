<?php

use App\Kernel;

if (file_exists(dirname(__DIR__).'/vendor/symfony/polyfill-php83/bootstrap.php')) {
    require_once dirname(__DIR__).'/vendor/symfony/polyfill-php83/bootstrap.php';
}

require_once dirname(__DIR__).'/vendor/autoload_runtime.php';

return static function (array $context) {
    return new Kernel($context['APP_ENV'], (bool) $context['APP_DEBUG']);
};
